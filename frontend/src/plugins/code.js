import { WASI, WASIContext, WASIWorkerHost } from '@runno/wasi'
import untar from 'js-untar'
import { downloadFileBlob } from '@/plugins/constants'

// CLANG, WASM BEGIN

// Cached constants
let cachedFs = null
let cachedClangUrl = null
let cachedWasmLd = null

function runnoFile(path, content) {
    return {
        timestamps: {
            access: new Date(),
            change: new Date(),
            modification: new Date(),
        },
        path: path,
        mode: 'binary',
        content: content
    }
}

async function prepareFS() {
    if (!cachedFs) {
        const sysrootFetch = await fetch("/sysroot.3.tar")
        const sysrootTar = await sysrootFetch.arrayBuffer()
        let fs = {}
        await untar(sysrootTar)
            .progress(function(extractedFile) {
                let p = "/" + extractedFile.name
                if(extractedFile.type === "5") { // directory
                    // do nothing
                }
                else if(extractedFile.type === "0") { // ordinary file
                    fs[p] = runnoFile(p, new Uint8Array(extractedFile.buffer))
                }
            })
        cachedFs = fs
    }
    return Object.assign({}, cachedFs)
}

async function fetchClang() {
    if (!cachedClangUrl) {
        const buffer = await (await fetch("/clang-15.2.wasm")).arrayBuffer()
        const fileBlob = new File([buffer], "/clang-15.2.wasm", {
            type: "application/wasm",
        })
        cachedClangUrl = URL.createObjectURL(fileBlob).toString()
    }
    return cachedClangUrl
}
async function fetchWasmLd() {
    if (!cachedWasmLd)
        cachedWasmLd = await (await fetch("/wasm-ld.wasm")).arrayBuffer()
    return cachedWasmLd
}
async function fetchAsArray(fileName) {
    const buffer = await (await fetch(fileName)).arrayBuffer()
    return new Uint8Array(buffer)
}

// === CLANG, WASM END

export const posixSignals = [
    '', 'SIGHUP', 'SIGINT', 'SIGQUIT', 'SIGILL', 'SIGTRAP', 'SIGABRT', 'SIGIOT',
    'SIGBUS', 'SIGFPE', 'SIGKILL', 'SIGUSR1', 'SIGSEGV', 'SIGUSR2', 'SIGPIPE',
    'SIGALRM', 'SIGTERM', 'SIGSTKFLT', 'SIGCHLD', 'SIGCONT', 'SIGSTOP',
    'SIGTSTP', 'SIGTTIN', 'SIGTTOU', 'SIGURG', 'SIGXCPU', 'SIGXFSZ',
    'SIGVTALRM', 'SIGPROF', 'SIGWINCH', 'SIGIO', 'SIGPWR', 'SIGSYS'
]
const clangCommonArgs = ['-isysroot', '/', '-isystem', '/include/c++/v1', '-isystem', '/include',
    '-isystem', '/lib/clang/16/include', '-fno-builtin', '-fexceptions', '-fwasm-exceptions', '-Wno-deprecated', '-std=c++20', '-D__TRAINER__']

const envC = atob(
    "I2luY2x1ZGUgPGN0eXBlLmg+CiNpbmNsdWRlIDxsaW1pdHMuaD4KI2luY2x1ZGUgPHN0ZGlvLmg+CiNpbmNsdWRlIDxzdGRsaWIuaD4KI2luY2x1ZGUgPGFzc2VydC5oPgojaW5jbHVkZSA8c3RyaW5nLmg+CiNpbmNsdWRlIDxtYXRoLmg+CiNpbmNsdWRlIDx0aW1lLmg+CgpuYW1lc3BhY2UgX19TVFVERU5UX05BTUVTUEFDRV9fIHsKICAgICNsaW5lIDEKICAgIF9fU1RVREVOVF9GSUxFX18KfQo="
)
const envCIO = atob(
    "I2luY2x1ZGUgPGN0eXBlLmg+CiNpbmNsdWRlIDxsaW1pdHMuaD4KI2luY2x1ZGUgPHN0ZGlvLmg+CiNpbmNsdWRlIDxzdGRsaWIuaD4KI2luY2x1ZGUgPGFzc2VydC5oPgojaW5jbHVkZSA8c3RyaW5nLmg+CiNpbmNsdWRlIDxtYXRoLmg+CiNpbmNsdWRlIDx0aW1lLmg+CgojaW5jbHVkZSAiL2xpYi9jL3RyYWluZXIuaCIKCnRlbXBsYXRlPGNsYXNzLi4uIEFyZ3M+IHN0YXRpYyBpbnQgdGVzdF9zY2FuZihjb25zdCBjaGFyICogZm10LCBBcmdzLi4uIGFyZ3MpIHsKICAgIHJldHVybiBmc2NhbmYoQ1RlYWNoZXI6OkNUZXN0ZXI6OnRlc3Rfc3RkaW4sIGZtdCwgYXJncy4uLik7Cn0KdGVtcGxhdGU8Y2xhc3MuLi4gQXJncz4gc3RhdGljIGludCB0ZXN0X3ByaW50Zihjb25zdCBjaGFyICogZm10LCBBcmdzLi4uIGFyZ3MpIHsKICAgIHJldHVybiBmcHJpbnRmKENUZWFjaGVyOjpDVGVzdGVyOjp0ZXN0X3N0ZG91dCwgZm10LCBhcmdzLi4uKTsKfQpzdGF0aWMgc3NpemVfdCB0ZXN0X2dldGxpbmUoY2hhciAqKiBsaW5lcHRyLCBzaXplX3QgKm4sIEZJTEUgKnN0cmVhbSkgewogICAgcmV0dXJuIGdldGxpbmUobGluZXB0ciwgbiwgQ1RlYWNoZXI6OkNUZXN0ZXI6OnRlc3Rfc3RkaW4pOwp9CnN0YXRpYyBzc2l6ZV90IHRlc3RfZ2V0ZGVsaW0oY2hhciAqKiBsaW5lcHRyLCBzaXplX3QgKm4sIGludCBkZWxpbSwgRklMRSAqc3RyZWFtKSB7CiAgICByZXR1cm4gZ2V0ZGVsaW0obGluZXB0ciwgbiwgZGVsaW0sIENUZWFjaGVyOjpDVGVzdGVyOjp0ZXN0X3N0ZGluKTsKfQpzdGF0aWMgaW50IHRlc3RfZmVvZihGSUxFICogc3RyZWFtKSB7CiAgICByZXR1cm4gZmVvZihDVGVhY2hlcjo6Q1Rlc3Rlcjo6dGVzdF9zdGRpbik7Cn0Kc3RhdGljIGludCB0ZXN0X2ZnZXRjKEZJTEUgKiBzdHJlYW0pIHsKICAgcmV0dXJuIGZnZXRjKENUZWFjaGVyOjpDVGVzdGVyOjp0ZXN0X3N0ZGluKTsKfQpzdGF0aWMgaW50IHRlc3RfZ2V0YyhGSUxFICogc3RyZWFtKSB7CiAgIHJldHVybiBnZXRjKENUZWFjaGVyOjpDVGVzdGVyOjp0ZXN0X3N0ZGluKTsKfQpzdGF0aWMgaW50IHRlc3RfdW5nZXRjKGludCBjaCwgRklMRSAqIHN0cmVhbSkgewogICByZXR1cm4gdW5nZXRjKGNoLCBDVGVhY2hlcjo6Q1Rlc3Rlcjo6dGVzdF9zdGRpbik7Cn0Kc3RhdGljIGludCB0ZXN0X2dldGNoYXIoKSB7CiAgcmV0dXJuIGdldGMoQ1RlYWNoZXI6OkNUZXN0ZXI6OnRlc3Rfc3RkaW4pOwp9CnN0YXRpYyBpbnQgdGVzdF9wdXRjaGFyKGludCB4KSB7CiAgcmV0dXJuIHB1dGMoeCwgQ1RlYWNoZXI6OkNUZXN0ZXI6OnRlc3Rfc3RkaW4pOwp9CgojZGVmaW5lIHNjYW5mIHRlc3Rfc2NhbmYKI2RlZmluZSBwcmludGYgdGVzdF9wcmludGYKI2RlZmluZSBnZXRsaW5lIHRlc3RfZ2V0bGluZQojZGVmaW5lIGdldGRlbGltIHRlc3RfZ2V0ZGVsaW0KI2RlZmluZSBmZW9mIHRlc3RfZmVvZgojZGVmaW5lIGZnZXRjIHRlc3RfZmdldGMKI2RlZmluZSBnZXRjIHRlc3RfZ2V0YwojZGVmaW5lIHVuZ2V0YyB0ZXN0X3VuZ2V0YwojZGVmaW5lIGdldGNoYXIgdGVzdF9nZXRjaGFyCiNkZWZpbmUgcHV0Y2hhciB0ZXN0X3B1dGNoYXIKCm5hbWVzcGFjZSBfX1NUVURFTlRfTkFNRVNQQUNFX18gewogICAgI2xpbmUgMQogICAgX19TVFVERU5UX0ZJTEVfXwogICAgI3VuZGVmIHNjYW5mCiAgICAjdW5kZWYgcHJpbnRmCiAgICAjdW5kZWYgZ2V0bGluZQogICAgI3VuZGVmIGdldGRlbGltCiAgICAjdW5kZWYgZmVvZgogICAgI3VuZGVmIGZnZXRjCiAgICAjdW5kZWYgZ2V0YwogICAgI3VuZGVmIHVuZ2V0YwogICAgI3VuZGVmIGdldGNoYXIKICAgICN1bmRlZiBwdXRjaGFyCn0K"
)
const envCpp = atob(
"I2luY2x1ZGUgPGN0eXBlLmg+CiNpbmNsdWRlIDxsaW1pdHMuaD4KI2luY2x1ZGUgPHN0ZGlvLmg+CiNpbmNsdWRlIDxzdGRsaWIuaD4KI2luY2x1ZGUgPGFzc2VydC5oPgojaW5jbHVkZSA8c3RyaW5nLmg+CiNpbmNsdWRlIDxtYXRoLmg+CiNpbmNsdWRlIDx0aW1lLmg+CgojaW5jbHVkZSA8aW9zdHJlYW0+CiNpbmNsdWRlIDxpb21hbmlwPgoKI2RlZmluZSB2ZWN0b3IgeHZlY3RvcgojZGVmaW5lIG1hcCB4bWFwCiNkZWZpbmUgc2V0IHhzZXQKI2RlZmluZSBxdWV1ZSB4cXVldWUKI2RlZmluZSBsaXN0IHhsaXN0CiNkZWZpbmUgc3RyaW5nIHhzdHJpbmcKCm5hbWVzcGFjZSBfX1NUVURFTlRfTkFNRVNQQUNFX18gewogICAgI2xpbmUgMQogICAgX19TVFVERU5UX0ZJTEVfXwogICAgI3VuZGVmIHZlY3RvcgogICAgI3VuZGVmIG1hcAogICAgI3VuZGVmIHNldAogICAgI3VuZGVmIHF1ZXVlCiAgICAjdW5kZWYgbGlzdAogICAgI3VuZGVmIHN0cmluZwp9Cg=="
)
const envCppStl = atob(
    "I2luY2x1ZGUgPGN0eXBlLmg+CiNpbmNsdWRlIDxsaW1pdHMuaD4KI2luY2x1ZGUgPHN0ZGlvLmg+CiNpbmNsdWRlIDxzdGRsaWIuaD4KI2luY2x1ZGUgPGFzc2VydC5oPgojaW5jbHVkZSA8c3RyaW5nLmg+CiNpbmNsdWRlIDxtYXRoLmg+CiNpbmNsdWRlIDx0aW1lLmg+CgojaW5jbHVkZSA8aW9zdHJlYW0+CiNpbmNsdWRlIDxpb21hbmlwPgojaW5jbHVkZSA8bWFwPgojaW5jbHVkZSA8dmVjdG9yPgojaW5jbHVkZSA8YWxnb3JpdGhtPgojaW5jbHVkZSA8c2V0PgojaW5jbHVkZSA8cXVldWU+CiNpbmNsdWRlIDxsaXN0PgojaW5jbHVkZSA8bWVtb3J5PgojaW5jbHVkZSA8c3RyaW5nPgoKbmFtZXNwYWNlIF9fU1RVREVOVF9OQU1FU1BBQ0VfXyB7CiAgICAjbGluZSAxCiAgICBfX1NUVURFTlRfRklMRV9fCn0K"
)

const libH = atob(
    "I2lmbmRlZiBfX1RFU1RFUl9fCiNkZWZpbmUgX19URVNURVJfXwoKI2luY2x1ZGUgPGN0eXBlLmg+CiNpbmNsdWRlIDxzdGRpby5oPgojaW5jbHVkZSA8c3RkbGliLmg+CiNpbmNsdWRlIDxhc3NlcnQuaD4KI2luY2x1ZGUgPHN0cmluZy5oPgojaW5jbHVkZSA8dGltZS5oPgoKbmFtZXNwYWNlIENUZWFjaGVyIHsKICAgIGNsYXNzIENUZXN0ZXJTdHJpbmcgewogICAgICBib29sIG1fRnJlZTsKICAgICAgY29uc3QgY2hhciAqIG1fU3RyOwogICAgcHVibGljOgogICAgICBDVGVzdGVyU3RyaW5nICggY29uc3QgY2hhciAqIHN0ciwgYm9vbCBmcmVlID0gZmFsc2UgKQogICAgICA6IG1fU3RyIChzdHIpLCBtX0ZyZWUgKGZyZWUpIHt9CiAgICAgIENUZXN0ZXJTdHJpbmcgKCBjaGFyICogc3RyLCBib29sIGZyZWUgPSB0cnVlICkKICAgICAgOiBtX1N0ciAoc3RyKSwgbV9GcmVlIChmcmVlKSB7fQogICAgICBDVGVzdGVyU3RyaW5nIChjb25zdCBDVGVzdGVyU3RyaW5nICYgb3RoZXIpCiAgICAgIDogbV9TdHIgKG90aGVyLm1fU3RyKSwgbV9GcmVlIChmYWxzZSkge30KICAgICAgfkNUZXN0ZXJTdHJpbmcgKCk7CiAgICAgIENUZXN0ZXJTdHJpbmcgJiBvcGVyYXRvciA9IChjb25zdCBDVGVzdGVyU3RyaW5nICYgb3RoZXIpOwogICAgICBjb25zdCBjaGFyICogc3RyICgpIHsgcmV0dXJuIG1fU3RyOyB9CiAgICB9OwogICAgI2RlZmluZSBTVFJfRU1QVFkgKChjb25zdCBjaGFyICopIE5VTEwpCiAgICAjZGVmaW5lIHN3cmFwKHN0cikgKENUZXN0ZXJTdHJpbmcoc3RyLCBmYWxzZSkpCiAgICBjbGFzcyBDVGVzdGVyIHsKICAgICAgICBzdGF0aWMgaW50IG1fUGFzc2VkLCBtX1RvdGFsOwogICAgICAgIHN0YXRpYyBjb25zdCBjaGFyICogbV9JbnB1dCwgKiBtX091dHB1dCwgKiBtX1JlZjsKCiAgICAgICAgc3RhdGljIGNvbnN0IGNoYXIgKiBpbnRUb1N0cmluZyAoIGNoYXIgKiBidWZmZXIsIGludCBsZW4gKTsKCiAgICAgICAgdHlwZWRlZiBzdHJ1Y3QgewogICAgICAgIAljb25zdCBjaGFyICoqIG1fRGF0YTsKICAgICAgICAJc2l6ZV90IG1fU2l6ZSwgbV9NYXg7CiAgICAgICAgfSBUQXJyOwogICAgICAgIHN0YXRpYyB2b2lkIGFwcGVuZExpbmUgKCBUQXJyICogYXJyLCBjb25zdCBjaGFyICogbGluZSApOwogICAgICAgIHN0YXRpYyBpbnQgY21wTGluZXMgKCBjaGFyICoqIGwsIGNoYXIgKiogciApOwogICAgICAgIHN0YXRpYyB2b2lkIGxvYWRMaW5lcyAoVEFyciAqIGFyciwgY2hhciAqIHN0cik7CiAgICBwdWJsaWM6CiAgICAJc3RhdGljIEZJTEUgKiB0ZXN0X3N0ZGluOwogICAgCXN0YXRpYyBGSUxFICogdGVzdF9zdGRvdXQ7CiAgICAgICAgc3RhdGljIGludCBhc3NlcnRCb29sICggaW50IGNvbmRpdGlvbiwgQ1Rlc3RlclN0cmluZyBpbnB1dCwgQ1Rlc3RlclN0cmluZyBvdXRwdXQgPSBTVFJfRU1QVFksIENUZXN0ZXJTdHJpbmcgcmVmID0gU1RSX0VNUFRZICk7CiAgICAgICAgc3RhdGljIGludCBhc3NlcnRFcXVhbCAoIENUZXN0ZXJTdHJpbmcgaW5wdXQsIENUZXN0ZXJTdHJpbmcgb3V0cHV0ID0gU1RSX0VNUFRZLCBDVGVzdGVyU3RyaW5nIHJlZiA9IFNUUl9FTVBUWSApOwogICAgICAgIHN0YXRpYyBpbnQgYXNzZXJ0RXF1YWwgKCBDVGVzdGVyU3RyaW5nIGlucHV0LCBpbnQgb3V0LCBpbnQgcmVmICk7CiAgICAgICAgc3RhdGljIHZvaWQgb3V0cHV0UHJpbnQgKCBjaGFyICogaW5wdXQsIGNoYXIgKiBvdXRwdXQgKTsKICAgICAgICBzdGF0aWMgaW50IGNvbXBhcmVMaW5lcyAoIGNvbnN0IGNoYXIgKiBsLCBjb25zdCBjaGFyICogciApOwogICAgICAgIGZyaWVuZCBpbnQgdGVzdHNQYXNzZWQoKTsKICAgICAgICBmcmllbmQgaW50IHRlc3RzVG90YWwoKTsKICAgICAgICBmcmllbmQgY29uc3QgY2hhciAqIHRlc3RJbnB1dCgpOwogICAgICAgIGZyaWVuZCBjb25zdCBjaGFyICogdGVzdE91dHB1dCgpOwogICAgICAgIGZyaWVuZCBjb25zdCBjaGFyICogdGVzdFJlZigpOwogICAgfTsKCiAgICAgICAgdGVtcGxhdGU8dHlwZW5hbWUgVCwgdHlwZW5hbWUgUiwgdHlwZW5hbWUuLi4gQXJncz4KCWNoYXIgKiBnZW5lcmF0ZU91dHB1dCAoIGNvbnN0IGNoYXIgKiBpbiwgc2l6ZV90IHN6T3V0LCBUIGZ1bmN0aW9uLCBSICogcmV0UHRyLCBBcmdzLi4uIGFyZ3MpIHsKCQlzaXplX3Qgc3pJbiA9IHN0cmxlbihpbik7CgkJY2hhciAqIG91dGJ1ZiA9IChjaGFyICopIG1hbGxvYyhzek91dCArIDEpOwoJCW91dGJ1ZlswXSA9ICdcMCc7CgkJQ1Rlc3Rlcjo6dGVzdF9zdGRpbiA9IGZtZW1vcGVuKGNvbnN0X2Nhc3Q8Y2hhciAqPihpbiksIHN6SW4sICJyIik7CgkJQ1Rlc3Rlcjo6dGVzdF9zdGRvdXQgPSBmbWVtb3BlbihvdXRidWYsIHN6T3V0LCAidyIpOwoJCVIgcmV0ID0gZnVuY3Rpb24oYXJncy4uLik7CgkJaWYgKHJldFB0cikgKnJldFB0ciA9IHJldDsKCQlmY2xvc2UoQ1Rlc3Rlcjo6dGVzdF9zdGRpbik7CgkJZmNsb3NlKENUZXN0ZXI6OnRlc3Rfc3Rkb3V0KTsKCgkJQ1Rlc3Rlcjo6dGVzdF9zdGRpbiA9IENUZXN0ZXI6OnRlc3Rfc3Rkb3V0ID0gMDsKCQlyZXR1cm4gb3V0YnVmOwoJfQogICAgICAgIHRlbXBsYXRlPHR5cGVuYW1lIFQsIHR5cGVuYW1lLi4uIEFyZ3M+CgljaGFyICogZ2VuZXJhdGVPdXRwdXQgKCBjb25zdCBjaGFyICogaW4sIHNpemVfdCBzek91dCwgVCBmdW5jdGlvbiwgQXJncy4uLiBhcmdzKSB7CgkJc2l6ZV90IHN6SW4gPSBzdHJsZW4oaW4pOwoJCWNoYXIgKiBvdXRidWYgPSAoY2hhciAqKSBtYWxsb2Moc3pPdXQgKyAxKTsKCQlvdXRidWZbMF0gPSAnXDAnOwoJCUNUZXN0ZXI6OnRlc3Rfc3RkaW4gPSBmbWVtb3Blbihjb25zdF9jYXN0PGNoYXIgKj4oaW4pLCBzekluLCAiciIpOwoJCUNUZXN0ZXI6OnRlc3Rfc3Rkb3V0ID0gZm1lbW9wZW4ob3V0YnVmLCBzek91dCwgInciKTsKCQlmdW5jdGlvbihhcmdzLi4uKTsKCQlmY2xvc2UoQ1Rlc3Rlcjo6dGVzdF9zdGRpbik7CgkJZmNsb3NlKENUZXN0ZXI6OnRlc3Rfc3Rkb3V0KTsKCgkJQ1Rlc3Rlcjo6dGVzdF9zdGRpbiA9IENUZXN0ZXI6OnRlc3Rfc3Rkb3V0ID0gMDsKCQlyZXR1cm4gb3V0YnVmOwoJfQp9CgojZW5kaWY="
)
const libC = atob(
    "I2luY2x1ZGUgPGN0eXBlLmg+CiNpbmNsdWRlIDxzdGRpby5oPgojaW5jbHVkZSA8c3RkbGliLmg+CiNpbmNsdWRlIDxhc3NlcnQuaD4KI2luY2x1ZGUgPHN0cmluZy5oPgojaW5jbHVkZSA8dGltZS5oPgojaW5jbHVkZSAiL2xpYi9jL3RyYWluZXIuaCIKCm5hbWVzcGFjZSBDVGVhY2hlciB7CglDVGVzdGVyU3RyaW5nOjp+Q1Rlc3RlclN0cmluZyAoKSB7IGlmIChtX0ZyZWUpIGZyZWUoKGNoYXIgKikgbV9TdHIpOyB9CiAgICBDVGVzdGVyU3RyaW5nICYgQ1Rlc3RlclN0cmluZzo6b3BlcmF0b3IgPSAoY29uc3QgQ1Rlc3RlclN0cmluZyAmIG90aGVyKSB7CiAgICAgICAgICBpZiAodGhpcyAhPSAmb3RoZXIpIHsKICAgICAgICAgICAgICBpZiAobV9GcmVlKSBmcmVlKChjaGFyICopIG1fU3RyKTsKICAgICAgICAgICAgICBtX0ZyZWUgPSBmYWxzZTsKICAgICAgICAgICAgICBtX1N0ciA9IG90aGVyLm1fU3RyOwogICAgICAgICAgfQogICAgICAgICAgcmV0dXJuICp0aGlzOwoJfQoJCiAgICAjZGVmaW5lIFNUUl9FTVBUWSAoKGNvbnN0IGNoYXIgKikgTlVMTCkKICAgICNkZWZpbmUgc3dyYXAoc3RyKSAoQ1Rlc3RlclN0cmluZyhzdHIsIGZhbHNlKSkKICAgIAogICAgY29uc3QgY2hhciAqIENUZXN0ZXI6OmludFRvU3RyaW5nICggY2hhciAqIGJ1ZmZlciwgaW50IGxlbiApIHsKICAgICAgICBidWZmZXJbMF0gPSAnXDAnOwogICAgICAgIHNucHJpbnRmKGJ1ZmZlciwgMzEsICIlZCIsIGxlbik7CiAgICAgICAgcmV0dXJuIGJ1ZmZlcjsKICAgIH0KICAgIHZvaWQgQ1Rlc3Rlcjo6YXBwZW5kTGluZSAoIFRBcnIgKiBhcnIsIGNvbnN0IGNoYXIgKiBsaW5lICkgewogICAgCWlmICggYXJyLT5tX1NpemUgPj0gYXJyLT5tX01heCApIHsKICAgIAkJYXJyLT5tX01heCArPSBhcnItPm1fTWF4ID8gMiAqIGFyci0+bV9NYXggOiAxOwogICAgCQlhcnItPm1fRGF0YSA9IChjb25zdCBjaGFyICoqKSByZWFsbG9jICggYXJyLT5tX0RhdGEsIHNpemVvZihhcnItPm1fRGF0YVswXSkgKiBhcnItPm1fTWF4ICk7CiAgICAJfQogICAgCWFyci0+bV9EYXRhW2Fyci0+bV9TaXplKytdID0gbGluZTsKICAgIH0KICAgIGludCBDVGVzdGVyOjpjbXBMaW5lcyAoIGNoYXIgKiogbCwgY2hhciAqKiByICkgewogICAgICAgIHJldHVybiBzdHJjbXAgKCAqbCwgKnIgKTsKICAgIH0KICAgIHZvaWQgQ1Rlc3Rlcjo6bG9hZExpbmVzIChUQXJyICogYXJyLCBjaGFyICogc3RyKSB7CiAgICAgICAgaWYgKCFzdHIpIHJldHVybjsKICAgIAljb25zdCBjaGFyICogbGluZVN0YXJ0ID0gTlVMTDsKICAgIAl3aGlsZSAoMSkgewogICAgCSAgICBpZiAoKnN0ciAmJiAqc3RyICE9ICdcbicpCiAgICAJICAgICAgICBsaW5lU3RhcnQgPSBsaW5lU3RhcnQgPyBsaW5lU3RhcnQgOiBzdHI7CiAgICAJICAgIGVsc2UgewogICAgCSAgICAgICAgaW50IHdpbGxFbmQgPSAhKCpzdHIpOwogICAgCSAgICAgICAgKnN0ciA9ICdcMCc7CiAgICAJICAgICAgICBpZiAobGluZVN0YXJ0KQogICAgCSAgICAgICAgICAgIGFwcGVuZExpbmUgKCBhcnIsIGxpbmVTdGFydCApOwogICAgICAgICAgICAgICAgaWYgKHdpbGxFbmQpCiAgICAgICAgICAgICAgICAgICAgYnJlYWs7CiAgICAJICAgICAgICBsaW5lU3RhcnQgPSBOVUxMOwogICAgCSAgICB9CiAgICAJICAgIHN0cisrOwogICAgCX0KICAgIAlxc29ydCAoIGFyci0+bV9EYXRhLCBhcnItPm1fU2l6ZSwgc2l6ZW9mKGFyci0+bV9EYXRhWzBdKSwgKCAoaW50ICgqKSAoY29uc3Qgdm9pZCAqLCBjb25zdCB2b2lkICopKSBDVGVzdGVyOjpjbXBMaW5lcyApICk7CiAgICB9CiAgICBpbnQgQ1Rlc3Rlcjo6YXNzZXJ0Qm9vbCAoIGludCBjb25kaXRpb24sIENUZXN0ZXJTdHJpbmcgaW5wdXQsIENUZXN0ZXJTdHJpbmcgb3V0cHV0LCBDVGVzdGVyU3RyaW5nIHJlZiApIHsKICAgICAgICBtX1Bhc3NlZCArPSAhIWNvbmRpdGlvbjsKICAgICAgICBtX1RvdGFsICs9IDE7CiAgICAgICAgaWYgKCBjb25kaXRpb24gKQogICAgICAgICAgICByZXR1cm4gMTsgLy8gZGVzdHJ1Y3RvciBjYWxsZWQKICAgICAgICBtX0lucHV0ID0gaW5wdXQuc3RyKCk7IG1fT3V0cHV0ID0gb3V0cHV0LnN0cigpOyBtX1JlZiA9IHJlZi5zdHIoKTsKICAgICAgICBhc3NlcnQoMCk7IC8vIGV4aXQKICAgIH0KICAgIGludCBDVGVzdGVyOjphc3NlcnRFcXVhbCAoIENUZXN0ZXJTdHJpbmcgaW5wdXQsIENUZXN0ZXJTdHJpbmcgb3V0cHV0LCBDVGVzdGVyU3RyaW5nIHJlZiApIHsKICAgICAgICBpbnQgZXF1YWwgPSAhc3RyY21wKG91dHB1dC5zdHIoKSwgcmVmLnN0cigpKTsKICAgICAgICByZXR1cm4gQ1Rlc3Rlcjo6YXNzZXJ0Qm9vbCAoIGVxdWFsLCBpbnB1dCwgb3V0cHV0LCByZWYgKTsKICAgIH0KICAgIHZvaWQgQ1Rlc3Rlcjo6b3V0cHV0UHJpbnQgKCBjaGFyICogaW5wdXQsIGNoYXIgKiBvdXRwdXQgKSB7CiAgICAgICAgbV9JbnB1dCA9IGlucHV0OwogICAgICAgIG1fT3V0cHV0ID0gb3V0cHV0OwogICAgICAgIG1fUGFzc2VkID0gbV9Ub3RhbCA9IDE7CiAgICB9CiAgICBpbnQgQ1Rlc3Rlcjo6YXNzZXJ0RXF1YWwgKCBDVGVzdGVyU3RyaW5nIGlucHV0LCBpbnQgb3V0LCBpbnQgcmVmICkgewogICAgICAgIGNoYXIgYnVmZmVyT3V0IFszMl0sIGJ1ZmZlclJlZiBbMzJdOwogICAgICAgIHJldHVybiBDVGVzdGVyOjphc3NlcnRFcXVhbCAoIGlucHV0LCBpbnRUb1N0cmluZyhidWZmZXJPdXQsIG91dCksIGludFRvU3RyaW5nKGJ1ZmZlclJlZiwgcmVmKSApOwogICAgfQogICAgaW50IENUZXN0ZXI6OmNvbXBhcmVMaW5lcyAoIGNvbnN0IGNoYXIgKiBsYywgY29uc3QgY2hhciAqIHJjICkgewogICAgICAgIFRBcnIgbGluZXNMID0ge30sIGxpbmVzUiA9IHt9OwogICAgICAgIGNoYXIgKiBsID0gc3RyZHVwKGxjKSwgKiByID0gc3RyZHVwKHJjKTsKICAgICAgICBsb2FkTGluZXMgKCAmbGluZXNMLCBsICk7CiAgICAgICAgbG9hZExpbmVzICggJmxpbmVzUiwgciApOwoKICAgICAgICBpZiAobGluZXNMLm1fU2l6ZSAhPSBsaW5lc1IubV9TaXplKSB7CiAgICAgICAgICAgIGNoYXIgYnVmZmVyT3V0IFszMl0sIGJ1ZmZlclJlZiBbMzJdOwogICAgICAgICAgICBDVGVzdGVyOjphc3NlcnRCb29sICggbGluZXNMLm1fU2l6ZSA9PSBsaW5lc1IubV9TaXplLCAiTGluZSBzaXplIG9mIHN0dWRlbnQgYW5kIHJlZiBzb2x1dGlvbiBkaWZmZXJzLiIsCiAgICAgICAgICAgICAgICBpbnRUb1N0cmluZyhidWZmZXJPdXQsIGxpbmVzTC5tX1NpemUpLCBpbnRUb1N0cmluZyhidWZmZXJSZWYsIGxpbmVzUi5tX1NpemUpICk7CiAgICAgICAgfQoKICAgICAgICBmb3IgKCBzaXplX3QgaSA9IDA7IGkgPCBsaW5lc0wubV9TaXplOyArK2kgKSB7CiAgICAgICAgICAgIGlmICggc3RyY21wKGxpbmVzTC5tX0RhdGFbaV0sIGxpbmVzUi5tX0RhdGFbaV0pICkKICAgICAgICAgICAgICAgIENUZXN0ZXI6OmFzc2VydEVxdWFsICggIlN0dWRlbnQgYW5kIHJlZiBsaW5lcyBkaWZmZXIiLCBzd3JhcChsaW5lc0wubV9EYXRhW2ldKSwgc3dyYXAobGluZXNSLm1fRGF0YVtpXSkgKTsKICAgICAgICB9CgogICAgICAgIENUZXN0ZXI6OmFzc2VydEJvb2wgKCAxLCAiTGluZXMgYXJlIG1hdGNoaW5nIiApOwoKICAgICAgICBmcmVlICggbGluZXNMLm1fRGF0YSApOwogICAgICAgIGZyZWUgKCBsaW5lc1IubV9EYXRhICk7CiAgICAgICAgZnJlZSAobCk7CiAgICAgICAgZnJlZSAocik7CiAgICAgICAgcmV0dXJuIDE7CiAgICB9CiAgICAKICAgIEZJTEUgKiBDVGVzdGVyOjp0ZXN0X3N0ZGluID0gTlVMTDsKICAgIEZJTEUgKiBDVGVzdGVyOjp0ZXN0X3N0ZG91dCA9IE5VTEw7ICAgIAoJaW50IENUZXN0ZXI6Om1fUGFzc2VkID0gMDsKCWludCBDVGVzdGVyOjptX1RvdGFsID0gMDsKCWNvbnN0IGNoYXIgKiBDVGVzdGVyOjptX0lucHV0ID0gTlVMTDsKCWNvbnN0IGNoYXIgKiBDVGVzdGVyOjptX091dHB1dCA9IE5VTEw7Cgljb25zdCBjaGFyICogQ1Rlc3Rlcjo6bV9SZWYgPSBOVUxMOwoJCglfX2F0dHJpYnV0ZV9fKChleHBvcnRfbmFtZSgidHJhaW5lcl90ZXN0c19wYXNzZWQiKSkpIGludCB0ZXN0c1Bhc3NlZCgpIHsKICAgICAgICByZXR1cm4gQ1Rlc3Rlcjo6bV9QYXNzZWQ7CiAgICB9CiAgICBfX2F0dHJpYnV0ZV9fKChleHBvcnRfbmFtZSgidHJhaW5lcl90ZXN0c190b3RhbCIpKSkgaW50IHRlc3RzVG90YWwoKSB7CiAgICAgICAgcmV0dXJuIENUZXN0ZXI6Om1fVG90YWw7CiAgICB9CiAgICBfX2F0dHJpYnV0ZV9fKChleHBvcnRfbmFtZSgidHJhaW5lcl9pbnB1dCIpKSkgY29uc3QgY2hhciAqIHRlc3RJbnB1dCgpIHsKICAgICAgICByZXR1cm4gQ1Rlc3Rlcjo6bV9JbnB1dDsKICAgIH0KICAgIF9fYXR0cmlidXRlX18oKGV4cG9ydF9uYW1lKCJ0cmFpbmVyX291dHB1dCIpKSkgY29uc3QgY2hhciAqIHRlc3RPdXRwdXQoKSB7CiAgICAgICAgcmV0dXJuIENUZXN0ZXI6Om1fT3V0cHV0OwogICAgfQogICAgX19hdHRyaWJ1dGVfXygoZXhwb3J0X25hbWUoInRyYWluZXJfcmVmIikpKSBjb25zdCBjaGFyICogdGVzdFJlZigpIHsKICAgICAgICByZXR1cm4gQ1Rlc3Rlcjo6bV9SZWY7CiAgICB9Cn0K"
)
const libHpp = atob(
    "I2lmbmRlZiBfX1RFU1RFUl9fCiNkZWZpbmUgX19URVNURVJfXwoKI2luY2x1ZGUgPGNhc3NlcnQ+CiNpbmNsdWRlIDxzdHJpbmc+CiNpbmNsdWRlIDxzZXQ+CiNpbmNsdWRlIDxpb3N0cmVhbT4KI2luY2x1ZGUgPHNzdHJlYW0+CgpuYW1lc3BhY2UgQ1RlYWNoZXIgewogICAgY2xhc3MgQ1Rlc3RlciB7CiAgICAgICAgc3RhdGljIGludCBtX1Bhc3NlZCwgbV9Ub3RhbDsKICAgICAgICBzdGF0aWMgc3RkOjpzdHJpbmcgbV9JbnB1dCwgbV9PdXRwdXQsIG1fUmVmOwoKICAgICAgICBzdGF0aWMgdm9pZCBsb2FkTGluZXMgKHN0ZDo6c2V0PHN0ZDo6c3RyaW5nPiAmIGxpbmVzLCBzdGQ6OnN0cmluZyBzdHIpOwogICAgcHVibGljOgogICAgCXN0YXRpYyBzdGQ6OmlzdHJpbmdzdHJlYW0gdGVzdF9zdGRpbjsKICAgIAlzdGF0aWMgc3RkOjpvc3RyaW5nc3RyZWFtIHRlc3Rfc3Rkb3V0OwogICAgICAgIHN0YXRpYyBib29sIGFzc2VydEJvb2wgKCBib29sIGNvbmRpdGlvbiwgY29uc3Qgc3RkOjpzdHJpbmcgJiBpbnB1dCwgY29uc3Qgc3RkOjpzdHJpbmcgJiBvdXRwdXQgPSAiIiwgY29uc3Qgc3RkOjpzdHJpbmcgJiByZWYgPSAiIiApOwogICAgICAgIHN0YXRpYyBib29sIGFzc2VydEVxdWFsICggY29uc3Qgc3RkOjpzdHJpbmcgJiBpbnB1dCwgY29uc3Qgc3RkOjpzdHJpbmcgJiBvdXRwdXQgPSAiIiwgY29uc3Qgc3RkOjpzdHJpbmcgJiByZWYgPSAiIiApOwogICAgICAgIHRlbXBsYXRlPHR5cGVuYW1lIFQ+CiAgICAgICAgc3RhdGljIGJvb2wgYXNzZXJ0RXF1YWwgKCBjb25zdCBzdGQ6OnN0cmluZyAmIGlucHV0LCBjb25zdCBUICYgb3V0LCBjb25zdCBUICYgcmVmICkgewogICAgICAgIAlzdGQ6Om9zdHJpbmdzdHJlYW0gb3NzLCByZWZPc3M7CiAgICAgICAgCW9zcyA8PCBvdXQgPDwgc3RkOjpmbHVzaDsKICAgICAgICAJcmVmT3NzIDw8IHJlZiA8PCBzdGQ6OmZsdXNoOwogICAgICAgIAlyZXR1cm4gQ1Rlc3Rlcjo6YXNzZXJ0RXF1YWwgKCBpbnB1dCwgb3NzLnN0cigpLCByZWZPc3Muc3RyKCkgKTsKICAgICAgICB9CiAgICAgICAgc3RhdGljIHZvaWQgb3V0cHV0UHJpbnQgKCBjb25zdCBzdGQ6OnN0cmluZyAmIGlucHV0LCBjb25zdCBzdGQ6OnN0cmluZyAmIG91dHB1dCApOyAgICAgICAgCiAgICAgICAgc3RhdGljIGJvb2wgY29tcGFyZUxpbmVzICggY29uc3Qgc3RkOjpzdHJpbmcgJiBsLCBjb25zdCBzdGQ6OnN0cmluZyAmIHIgKTsKICAgICAgICBmcmllbmQgaW50IHRlc3RzUGFzc2VkKCk7CiAgICAgICAgZnJpZW5kIGludCB0ZXN0c1RvdGFsKCk7CiAgICAgICAgZnJpZW5kIGNvbnN0IGNoYXIgKiB0ZXN0SW5wdXQoKTsKICAgICAgICBmcmllbmQgY29uc3QgY2hhciAqIHRlc3RPdXRwdXQoKTsKICAgICAgICBmcmllbmQgY29uc3QgY2hhciAqIHRlc3RSZWYoKTsKICAgIH07CgogICAgdGVtcGxhdGU8dHlwZW5hbWUgVCwgdHlwZW5hbWUgUiwgdHlwZW5hbWUuLi4gQXJncz4KCXN0ZDo6c3RyaW5nIGdlbmVyYXRlT3V0cHV0ICggY29uc3Qgc3RkOjpzdHJpbmcgJiBpbiwgVCBmdW5jdGlvbiwgUiAmIHJldFJlZiwgQXJncy4uLiBhcmdzICkgewoJCWF1dG8gaW5CdWYgPSBzdGQ6OmNpbi5yZGJ1ZigpOwoJCUNUZXN0ZXI6OnRlc3Rfc3RkaW4gPSBzdGQ6OmlzdHJpbmdzdHJlYW0oaW4pOwoJCXN0ZDo6Y2luLnJkYnVmKENUZXN0ZXI6OnRlc3Rfc3RkaW4ucmRidWYoKSk7CgkJCgkJYXV0byBvdXRCdWYgPSBzdGQ6OmNvdXQucmRidWYoKTsKCQlDVGVzdGVyOjp0ZXN0X3N0ZG91dC5zdHIoIiIpOwoJCUNUZXN0ZXI6OnRlc3Rfc3Rkb3V0LmNsZWFyKCk7CgkJc3RkOjpjb3V0LnJkYnVmKENUZXN0ZXI6OnRlc3Rfc3Rkb3V0LnJkYnVmKCkpOwkJCgkJCgkJcmV0UmVmID0gZnVuY3Rpb24oYXJncy4uLik7CgkJCgkJc3RkOjpjaW4ucmRidWYoaW5CdWYpOwoJCXN0ZDo6Y291dC5yZGJ1ZihvdXRCdWYpOwoJCXJldHVybiBDVGVzdGVyOjp0ZXN0X3N0ZG91dC5zdHIoKTsKCX0KICAgIHRlbXBsYXRlPHR5cGVuYW1lIFQsIHR5cGVuYW1lLi4uIEFyZ3M+CglzdGQ6OnN0cmluZyBnZW5lcmF0ZU91dHB1dCAoIGNvbnN0IHN0ZDo6c3RyaW5nICYgaW4sIFQgZnVuY3Rpb24sIEFyZ3MuLi4gYXJncyApIHsKCQlhdXRvIGluQnVmID0gc3RkOjpjaW4ucmRidWYoKTsKCQlDVGVzdGVyOjp0ZXN0X3N0ZGluID0gc3RkOjppc3RyaW5nc3RyZWFtKGluKTsKCQlzdGQ6OmNpbi5yZGJ1ZihDVGVzdGVyOjp0ZXN0X3N0ZGluLnJkYnVmKCkpOwoJCQoJCWF1dG8gb3V0QnVmID0gc3RkOjpjb3V0LnJkYnVmKCk7CgkJQ1Rlc3Rlcjo6dGVzdF9zdGRvdXQuc3RyKCIiKTsKCQlDVGVzdGVyOjp0ZXN0X3N0ZG91dC5jbGVhcigpOwoJCXN0ZDo6Y291dC5yZGJ1ZihDVGVzdGVyOjp0ZXN0X3N0ZG91dC5yZGJ1ZigpKTsJCQoJCQoJCWZ1bmN0aW9uKGFyZ3MuLi4pOwoJCQoJCXN0ZDo6Y2luLnJkYnVmKGluQnVmKTsKCQlzdGQ6OmNvdXQucmRidWYob3V0QnVmKTsKCQlyZXR1cm4gQ1Rlc3Rlcjo6dGVzdF9zdGRvdXQuc3RyKCk7Cgl9CQp9CgojZW5kaWY="
)
const libCpp = atob(
    "I2luY2x1ZGUgIi9saWIvY3BwL3RyYWluZXIuaHBwIgoKbmFtZXNwYWNlIENUZWFjaGVyIHsKICAgIHZvaWQgQ1Rlc3Rlcjo6bG9hZExpbmVzIChzdGQ6OnNldDxzdGQ6OnN0cmluZz4gJiBsaW5lcywgc3RkOjpzdHJpbmcgc3RyKSB7CiAgICAJYXV0byBpdCA9IHN0ci5iZWdpbigpOwogICAgCXNpemVfdCBsaW5lU3RhcnQgPSAtMTsgCiAgICAJd2hpbGUgKDEpIHsKICAgIAkgICAgaWYgKCppdCAmJiAqaXQgIT0gJ1xuJykKICAgIAkgICAgICAgIGxpbmVTdGFydCA9IGxpbmVTdGFydCAhPSAtMSA/IGxpbmVTdGFydCA6IGl0IC0gc3RyLmJlZ2luKCk7CiAgICAJICAgIGVsc2UgewogICAgCSAgICAgICAgYm9vbCB3aWxsRW5kID0gaXQgPT0gc3RyLmVuZCgpOwogICAgCSAgICAgICAgaWYgKCF3aWxsRW5kKQogICAgCSAgICAgICAgCSppdCA9ICdcMCc7CiAgICAJICAgICAgICBpZiAobGluZVN0YXJ0ICE9IC0xKQogICAgCSAgICAgICAgICAgIGxpbmVzLmluc2VydCAoIHN0ci5zdWJzdHIobGluZVN0YXJ0KSApOwogICAgICAgICAgICAgICAgaWYgKHdpbGxFbmQpCiAgICAgICAgICAgICAgICAgICAgYnJlYWs7CiAgICAJICAgICAgICBsaW5lU3RhcnQgPSAtMTsKICAgIAkgICAgfQogICAgCSAgICBpdCsrOwogICAgCX0KICAgIH0KICAgIGJvb2wgQ1Rlc3Rlcjo6YXNzZXJ0Qm9vbCAoIGJvb2wgY29uZGl0aW9uLCBjb25zdCBzdGQ6OnN0cmluZyAmIGlucHV0LCBjb25zdCBzdGQ6OnN0cmluZyAmIG91dHB1dCwgY29uc3Qgc3RkOjpzdHJpbmcgJiByZWYgKSB7CiAgICAgICAgbV9QYXNzZWQgKz0gISFjb25kaXRpb247CiAgICAgICAgbV9Ub3RhbCArPSAxOwogICAgICAgIGlmICggY29uZGl0aW9uICkKICAgICAgICAgICAgcmV0dXJuIDE7IC8vIGRlc3RydWN0b3IgY2FsbGVkCiAgICAgICAgbV9JbnB1dCA9IGlucHV0OwogICAgICAgIG1fT3V0cHV0ID0gb3V0cHV0OwogICAgICAgIG1fUmVmID0gcmVmOwogICAgICAgIGFzc2VydCgwKTsgLy8gZXhpdAogICAgfQogICAgYm9vbCBDVGVzdGVyOjphc3NlcnRFcXVhbCAoIGNvbnN0IHN0ZDo6c3RyaW5nICYgaW5wdXQsIGNvbnN0IHN0ZDo6c3RyaW5nICYgb3V0cHV0LCBjb25zdCBzdGQ6OnN0cmluZyAmIHJlZiApIHsKICAgICAgICByZXR1cm4gQ1Rlc3Rlcjo6YXNzZXJ0Qm9vbCAoIG91dHB1dCA9PSByZWYsIGlucHV0LCBvdXRwdXQsIHJlZiApOwogICAgfQogICAgCiAgICB2b2lkIENUZXN0ZXI6Om91dHB1dFByaW50ICggY29uc3Qgc3RkOjpzdHJpbmcgJiBpbnB1dCwgY29uc3Qgc3RkOjpzdHJpbmcgJiBvdXRwdXQgKSB7CgkJbV9JbnB1dCA9IGlucHV0OwogICAgICAgIG1fT3V0cHV0ID0gb3V0cHV0OwogICAgICAgIG1fUGFzc2VkID0gbV9Ub3RhbCA9IDE7CiAgICB9CgogICAgYm9vbCBDVGVzdGVyOjpjb21wYXJlTGluZXMgKCBjb25zdCBzdGQ6OnN0cmluZyAmIGwsIGNvbnN0IHN0ZDo6c3RyaW5nICYgciApIHsKICAgICAgICBzdGQ6OnNldDxzdGQ6OnN0cmluZz4gbGluZXNMLCBsaW5lc1I7CiAgICAgICAgbG9hZExpbmVzICggbGluZXNMLCBsICk7CiAgICAgICAgbG9hZExpbmVzICggbGluZXNSLCByICk7CgogICAgICAgIGlmIChsaW5lc0wuc2l6ZSgpICE9IGxpbmVzUi5zaXplKCkpIHsKICAgICAgICAJc3RkOjpvc3RyaW5nc3RyZWFtIG9zcywgcmVmT3NzOwogICAgICAgIAlvc3MgPDwgbGluZXNMLnNpemUoKSA8PCBzdGQ6OmZsdXNoOwogICAgICAgIAlyZWZPc3MgPDwgbGluZXNMLnNpemUoKSA8PCBzdGQ6OmZsdXNoOwogICAgICAgICAgICBDVGVzdGVyOjphc3NlcnRFcXVhbCAoICJMaW5lIHNpemUgb2Ygc3R1ZGVudCBhbmQgcmVmIHNvbHV0aW9uIGRpZmZlcnMuIiwgbGluZXNMLnNpemUoKSwgbGluZXNSLnNpemUoKSApOwogICAgICAgIH0KCgkJYXV0byBpdFIgPSBsaW5lc1IuYmVnaW4oKTsKCQlmb3IgKCBhdXRvIGl0TCA9IGxpbmVzTC5iZWdpbigpOyBpdEwgIT0gbGluZXNMLmVuZCgpOyArK2l0TCApIHsKCQkJaWYgKCppdEwgIT0gKml0UikKCQkJCUNUZXN0ZXI6OmFzc2VydEVxdWFsICggIlN0dWRlbnQgYW5kIHJlZiBsaW5lcyBkaWZmZXIiLCAqaXRMLCAqaXRSICk7CgkJCSsraXRSOwoJCX0KCiAgICAgICAgQ1Rlc3Rlcjo6YXNzZXJ0Qm9vbCAoIDEsICJMaW5lcyBhcmUgbWF0Y2hpbmciICk7CiAgICAgICAgcmV0dXJuIDE7CiAgICB9CiAgICAKICAgIHN0ZDo6aXN0cmluZ3N0cmVhbSBDVGVzdGVyOjp0ZXN0X3N0ZGluOwogICAgc3RkOjpvc3RyaW5nc3RyZWFtIENUZXN0ZXI6OnRlc3Rfc3Rkb3V0OwoJaW50IENUZXN0ZXI6Om1fUGFzc2VkID0gMDsKCWludCBDVGVzdGVyOjptX1RvdGFsID0gMDsKCXN0ZDo6c3RyaW5nIENUZXN0ZXI6Om1fSW5wdXQ7CglzdGQ6OnN0cmluZyBDVGVzdGVyOjptX091dHB1dDsKCXN0ZDo6c3RyaW5nIENUZXN0ZXI6Om1fUmVmOwoJCglfX2F0dHJpYnV0ZV9fKChleHBvcnRfbmFtZSgidHJhaW5lcl90ZXN0c19wYXNzZWQiKSkpIGludCB0ZXN0c1Bhc3NlZCgpIHsKICAgICAgICByZXR1cm4gQ1Rlc3Rlcjo6bV9QYXNzZWQ7CiAgICB9CiAgICBfX2F0dHJpYnV0ZV9fKChleHBvcnRfbmFtZSgidHJhaW5lcl90ZXN0c190b3RhbCIpKSkgaW50IHRlc3RzVG90YWwoKSB7CiAgICAgICAgcmV0dXJuIENUZXN0ZXI6Om1fVG90YWw7CiAgICB9CiAgICBfX2F0dHJpYnV0ZV9fKChleHBvcnRfbmFtZSgidHJhaW5lcl9pbnB1dCIpKSkgY29uc3QgY2hhciAqIHRlc3RJbnB1dCgpIHsKICAgICAgICByZXR1cm4gQ1Rlc3Rlcjo6bV9JbnB1dC5jX3N0cigpOwogICAgfQogICAgX19hdHRyaWJ1dGVfXygoZXhwb3J0X25hbWUoInRyYWluZXJfb3V0cHV0IikpKSBjb25zdCBjaGFyICogdGVzdE91dHB1dCgpIHsKICAgICAgICByZXR1cm4gQ1Rlc3Rlcjo6bV9PdXRwdXQuY19zdHIoKTsKICAgIH0KICAgIF9fYXR0cmlidXRlX18oKGV4cG9ydF9uYW1lKCJ0cmFpbmVyX3JlZiIpKSkgY29uc3QgY2hhciAqIHRlc3RSZWYoKSB7CiAgICAgICAgcmV0dXJuIENUZXN0ZXI6Om1fUmVmLmNfc3RyKCk7CiAgICB9Cn0K"
)

const stackSize = 1024 * 1024 // 1 MB stack
const libdir = '/lib/wasm32-wasi'
const crt1 = `${libdir}/crt1.o`

const LIB_H_FILE   = '/lib/c/trainer.h'
const LIB_C_FILE   = '/lib/c/trainer.cpp'
const LIB_O_FILE   = '/lib/c/trainer.o'
const LIB_HPP_FILE = '/lib/cpp/trainer.hpp'
const LIB_CPP_FILE = '/lib/cpp/trainer.cpp'
const LIB_OPP_FILE = '/lib/cpp/trainer.o'
const TEACHER_FILE = '/teacher.cpp'
const TEACHER_OBJ  = '/teacher.o'
const WASM_FILE    = '/test.wasm'
const DEFAULT_TIMEOUT = 0 // no timeout

const compileFile = async (fs, codeFile, objFile, appendOutput) => {
    let context = new WASIContext({
        args: [ 'clang++', ...clangCommonArgs, '-c', codeFile, '-o', objFile ],
        env: {},
        // stdin: undefined,
        stdout: appendOutput,
        stderr: appendOutput,
        fs: fs,
        // debug: (name, args, ret, data) => { console.log({name, args, ret, data}) }
    })

    let worker = new WASIWorkerHost(await fetchClang(), null, context)
    console.log(`Starting worker for ${codeFile}`)
    return worker.start()
        .then((result) => {
            console.log(`Finished worker for ${codeFile}`)
            return result
        })
}
export const getEnvelope = (codeData) => {
    switch (codeData.envelopeType) {
        case 'ENV_C':
            return envC
        case 'ENV_C_IO':
            return envCIO
        case 'ENV_CPP':
            return envCpp
        case 'ENV_CPP_STL':
            return envCppStl
        case 'ENV_CUSTOM':
            return codeData.customEnvelope
        default:
            return '__STUDENT_FILE__'
    }
}
export const compile = async (codeData) => {
    let fs = await prepareFS()
    if (codeData.libraryType === 'LIB_C')
        fs[LIB_H_FILE] = runnoFile(LIB_H_FILE, new TextEncoder().encode(libH))
    if (codeData.libraryType === 'LIB_CPP')
        fs[LIB_HPP_FILE] = runnoFile(LIB_HPP_FILE, new TextEncoder().encode(libHpp))

    const envelope = getEnvelope(codeData)
    let compileQueue = []

    // Student files
    for (let file of codeData.files) {
        const path = `/${file.name}`
        const objPath = `/${file.name}.o`
        const content = file.headerFile ? file.content : envelope
            .replace(/__STUDENT_FILE__/, file.content)
            .replace(/__STUDENT_NAMESPACE__/, 'CStudent')
        fs[path] = runnoFile(path, new TextEncoder().encode(content))
        if (!file.headerFile && file.content.length)
            compileQueue.push([path, objPath])
    }

    let promises = []
    let output = ""
    let appendOutput
    if (codeData.hideCompilerOutput === true) {
       appendOutput = () => {} 
    } else {
       appendOutput = (s) => { output += s }
    }

    while (compileQueue.length) {
        const [codeFile, objFile] = compileQueue.pop()
        promises.push(compileFile(fs, codeFile, objFile, appendOutput)
            .then((result) => {
                if (result.exitCode !== 0)
                    throw new Error(output)
                return result.fs[objFile]
            }))
    }

    // Ref files
    for (let file of codeData.files) {
        const path = `/ref/${file.name}`
        const objPath = `/ref/${file.name}.o`
        const refContent = file.headerFile ? file.reference : envelope
            .replace(/__STUDENT_FILE__/, file.reference)
            .replace(/__STUDENT_NAMESPACE__/, 'CRef')
        fs[path] = runnoFile(path, new TextEncoder().encode(refContent))
        if (!file.headerFile && file.reference.length)
            compileQueue.push([path, objPath])
    }

    // Teacher file
    fs[TEACHER_FILE] = runnoFile(TEACHER_FILE, new TextEncoder().encode(codeData.codeHidden))
    compileQueue.push([TEACHER_FILE, TEACHER_OBJ])

    // Library
    if (codeData.libraryType === 'LIB_C') {
        fs[LIB_C_FILE] = runnoFile(LIB_C_FILE, new TextEncoder().encode(libC))
        fs[LIB_O_FILE] = runnoFile(LIB_O_FILE, await fetchAsArray("/libc.1.o"))
        // compileQueue.push([LIB_C_FILE, LIB_O_FILE])
    }
    if (codeData.libraryType === 'LIB_CPP') {
        fs[LIB_CPP_FILE] = runnoFile(LIB_CPP_FILE, new TextEncoder().encode(libCpp))
        fs[LIB_OPP_FILE] = runnoFile(LIB_OPP_FILE, await fetchAsArray("/libcpp.1.o"))
        // compileQueue.push([LIB_CPP_FILE, LIB_OPP_FILE])
    }

    while (compileQueue.length) {
        const [codeFile, objFile] = compileQueue.pop()
        promises.push(compileFile(fs, codeFile, objFile, (s) => { output += s })
            .then((result) => {
                if (result.exitCode !== 0)
                    throw new Error(output)
                return result.fs[objFile]
            }))
    }

    let files = await Promise.all(promises)
    for (let file of files) {
        fs[file.path] = file
        if (file.path === LIB_O_FILE || file.path === LIB_OPP_FILE) // download lib (cache)
            await downloadFileBlob(new Blob([file.content], { type: "application/octet-stream" }), file.path)
    }
    return [output, fs]
}
export const link = async (fs, codeData) => {
    let [stdout, stderr] = ['', '']
    let objects = [TEACHER_OBJ]
    if (codeData.libraryType === 'LIB_C')
        objects.push(LIB_O_FILE)
    if (codeData.libraryType === 'LIB_CPP')
        objects.push(LIB_OPP_FILE)
    for (let file of codeData.files)
        if (!file.headerFile) {
            if (file.content.length) objects.push(`/${file.name}.o`)
            if (file.reference.length) objects.push(`/ref/${file.name}.o`)
        }
    let context = new WASIContext({
        env: {},
        args: ['wasm-ld', '-z', `stack-size=${stackSize}`, `-L${libdir}`, crt1, ...objects,
            '-lc', '-lc++', '-lc++abi', '-lunwind', '-L/lib/clang/16/lib/wasi',
            '-lclang_rt.builtins-wasm32', '-o', WASM_FILE],
        fs: fs,
        // stdin: undefined,
        stdout: (s) => { stdout += s },
        stderr: (s) => { stderr += s },
        // debug: (name, args, ret, data) => { console.log({name, args, ret, data}) }
    })

    let wasi = new WASI(context)
    wasi.init(await WebAssembly.instantiate(await fetchWasmLd(), {
        wasi_snapshot_preview1: wasi.getImports("preview1"),
        wasi_unstable: wasi.getImports("unstable")
    }))
    let result = await wasi.start()
    return [stdout, stderr, result.exitCode === 0 ? result.fs[WASM_FILE].content : null]
}
export const run = async (wasmFileContent, appendOutput, finish, extraArgs = [], timeout = DEFAULT_TIMEOUT) => {
    let args = [ './a.out' ].concat(extraArgs)
    let context = new WASIContext({
        env: {},
        args: args,
        fs: {},
        // isTTY: true
        stdout: (s) => { appendOutput(s) },
        stderr: (s) => { appendOutput(s) },
        // debug: (name, args, ret, data) => { console.log({name, args, ret, data}) }
    })

    let workerHost = new WASIWorkerHost(null, wasmFileContent, context);
    let program_timed_out = false
    let timeoutFn = timeout > 0 ? setTimeout(() => {
        program_timed_out = true
        workerHost.kill()
    }, timeout * 1000) : undefined
    workerHost.start()
        .then((result) => {
            clearTimeout(timeoutFn)
            finish(result)
        })
        .catch((err) => {
            console.log(err.message)
            appendOutput(`${err}\n`)
            clearTimeout(timeoutFn)
            finish(null, program_timed_out)
        })
    return workerHost
}

// === CODE END
