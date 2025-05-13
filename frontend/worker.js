var ut = Object.defineProperty;
var gt = (r, T, D) => T in r ? ut(r, T, {
    enumerable: !0,
    configurable: !0,
    writable: !0,
    value: D
}) : r[T] = D;
var d = (r, T, D) => (gt(r, typeof T != "symbol" ? T + "" : T, D), D);
(function() {
    "use strict";
    var r = (e => (e[e.SUCCESS = 0] = "SUCCESS", e[e.E2BIG = 1] = "E2BIG", e[e.EACCESS = 2] = "EACCESS", e[e.EADDRINUSE = 3] = "EADDRINUSE", e[e.EADDRNOTAVAIL = 4] = "EADDRNOTAVAIL", e[e.EAFNOSUPPORT = 5] = "EAFNOSUPPORT", e[e.EAGAIN = 6] = "EAGAIN", e[e.EALREADY = 7] = "EALREADY", e[e.EBADF = 8] = "EBADF", e[e.EBADMSG = 9] = "EBADMSG", e[e.EBUSY = 10] = "EBUSY", e[e.ECANCELED = 11] = "ECANCELED", e[e.ECHILD = 12] = "ECHILD", e[e.ECONNABORTED = 13] = "ECONNABORTED", e[e.ECONNREFUSED = 14] = "ECONNREFUSED", e[e.ECONNRESET = 15] = "ECONNRESET", e[e.EDEADLK = 16] = "EDEADLK", e[e.EDESTADDRREQ = 17] = "EDESTADDRREQ", e[e.EDOM = 18] = "EDOM", e[e.EDQUOT = 19] = "EDQUOT", e[e.EEXIST = 20] = "EEXIST", e[e.EFAULT = 21] = "EFAULT", e[e.EFBIG = 22] = "EFBIG", e[e.EHOSTUNREACH = 23] = "EHOSTUNREACH", e[e.EIDRM = 24] = "EIDRM", e[e.EILSEQ = 25] = "EILSEQ", e[e.EINPROGRESS = 26] = "EINPROGRESS", e[e.EINTR = 27] = "EINTR", e[e.EINVAL = 28] = "EINVAL", e[e.EIO = 29] = "EIO", e[e.EISCONN = 30] = "EISCONN", e[e.EISDIR = 31] = "EISDIR", e[e.ELOOP = 32] = "ELOOP", e[e.EMFILE = 33] = "EMFILE", e[e.EMLINK = 34] = "EMLINK", e[e.EMSGSIZE = 35] = "EMSGSIZE", e[e.EMULTIHOP = 36] = "EMULTIHOP", e[e.ENAMETOOLONG = 37] = "ENAMETOOLONG", e[e.ENETDOWN = 38] = "ENETDOWN", e[e.ENETRESET = 39] = "ENETRESET", e[e.ENETUNREACH = 40] = "ENETUNREACH", e[e.ENFILE = 41] = "ENFILE", e[e.ENOBUFS = 42] = "ENOBUFS", e[e.ENODEV = 43] = "ENODEV", e[e.ENOENT = 44] = "ENOENT", e[e.ENOEXEC = 45] = "ENOEXEC", e[e.ENOLCK = 46] = "ENOLCK", e[e.ENOLINK = 47] = "ENOLINK", e[e.ENOMEM = 48] = "ENOMEM", e[e.ENOMSG = 49] = "ENOMSG", e[e.ENOPROTOOPT = 50] = "ENOPROTOOPT", e[e.ENOSPC = 51] = "ENOSPC", e[e.ENOSYS = 52] = "ENOSYS", e[e.ENOTCONN = 53] = "ENOTCONN", e[e.ENOTDIR = 54] = "ENOTDIR", e[e.ENOTEMPTY = 55] = "ENOTEMPTY", e[e.ENOTRECOVERABLE = 56] = "ENOTRECOVERABLE", e[e.ENOTSOCK = 57] = "ENOTSOCK", e[e.ENOTSUP = 58] = "ENOTSUP", e[e.ENOTTY = 59] = "ENOTTY", e[e.ENXIO = 60] = "ENXIO", e[e.EOVERFLOW = 61] = "EOVERFLOW", e[e.EOWNERDEAD = 62] = "EOWNERDEAD", e[e.EPERM = 63] = "EPERM", e[e.EPIPE = 64] = "EPIPE", e[e.EPROTO = 65] = "EPROTO", e[e.EPROTONOSUPPORT = 66] = "EPROTONOSUPPORT", e[e.EPROTOTYPE = 67] = "EPROTOTYPE", e[e.ERANGE = 68] = "ERANGE", e[e.EROFS = 69] = "EROFS", e[e.ESPIPE = 70] = "ESPIPE", e[e.ESRCH = 71] = "ESRCH", e[e.ESTALE = 72] = "ESTALE", e[e.ETIMEDOUT = 73] = "ETIMEDOUT", e[e.ETXTBSY = 74] = "ETXTBSY", e[e.EXDEV = 75] = "EXDEV", e[e.ENOTCAPABLE = 76] = "ENOTCAPABLE", e))(r || {}),
        T = (e => (e[e.REALTIME = 0] = "REALTIME", e[e.MONOTONIC = 1] = "MONOTONIC", e[e.PROCESS_CPUTIME_ID = 2] = "PROCESS_CPUTIME_ID", e[e.THREAD_CPUTIME_ID = 3] = "THREAD_CPUTIME_ID", e))(T || {}),
        D = (e => (e[e.SET = 0] = "SET", e[e.CUR = 1] = "CUR", e[e.END = 2] = "END", e))(D || {}),
        A = (e => (e[e.UNKNOWN = 0] = "UNKNOWN", e[e.BLOCK_DEVICE = 1] = "BLOCK_DEVICE", e[e.CHARACTER_DEVICE = 2] = "CHARACTER_DEVICE", e[e.DIRECTORY = 3] = "DIRECTORY", e[e.REGULAR_FILE = 4] = "REGULAR_FILE", e[e.SOCKET_DGRAM = 5] = "SOCKET_DGRAM", e[e.SOCKET_STREAM = 6] = "SOCKET_STREAM", e[e.SYMBOLIC_LINK = 7] = "SYMBOLIC_LINK", e))(A || {}),
        G = (e => (e[e.DIR = 0] = "DIR", e))(G || {}),
        b = (e => (e[e.CLOCK = 0] = "CLOCK", e[e.FD_READ = 1] = "FD_READ", e[e.FD_WRITE = 2] = "FD_WRITE", e))(b || {});
    const N = {
            CREAT: 1,
            DIRECTORY: 2,
            EXCL: 4,
            TRUNC: 8
        },
        O = {
            APPEND: 1,
            DSYNC: 2,
            NONBLOCK: 4,
            RSYNC: 8,
            SYNC: 16
        },
        _ = {
            FD_DATASYNC: BigInt(1) << BigInt(0),
            FD_READ: BigInt(1) << BigInt(1),
            FD_SEEK: BigInt(1) << BigInt(2),
            FD_FDSTAT_SET_FLAGS: BigInt(1) << BigInt(3),
            FD_SYNC: BigInt(1) << BigInt(4),
            FD_TELL: BigInt(1) << BigInt(5),
            FD_WRITE: BigInt(1) << BigInt(6),
            FD_ADVISE: BigInt(1) << BigInt(7),
            FD_ALLOCATE: BigInt(1) << BigInt(8),
            PATH_CREATE_DIRECTORY: BigInt(1) << BigInt(9),
            PATH_CREATE_FILE: BigInt(1) << BigInt(10),
            PATH_LINK_SOURCE: BigInt(1) << BigInt(11),
            PATH_LINK_TARGET: BigInt(1) << BigInt(12),
            PATH_OPEN: BigInt(1) << BigInt(13),
            FD_READDIR: BigInt(1) << BigInt(14),
            PATH_READLINK: BigInt(1) << BigInt(15),
            PATH_RENAME_SOURCE: BigInt(1) << BigInt(16),
            PATH_RENAME_TARGET: BigInt(1) << BigInt(17),
            PATH_FILESTAT_GET: BigInt(1) << BigInt(18),
            PATH_FILESTAT_SET_SIZE: BigInt(1) << BigInt(19),
            PATH_FILESTAT_SET_TIMES: BigInt(1) << BigInt(20),
            FD_FILESTAT_GET: BigInt(1) << BigInt(21),
            FD_FILESTAT_SET_SIZE: BigInt(1) << BigInt(22),
            FD_FILESTAT_SET_TIMES: BigInt(1) << BigInt(23),
            PATH_SYMLINK: BigInt(1) << BigInt(24),
            PATH_REMOVE_DIRECTORY: BigInt(1) << BigInt(25),
            PATH_UNLINK_FILE: BigInt(1) << BigInt(26),
            POLL_FD_READWRITE: BigInt(1) << BigInt(27),
            SOCK_SHUTDOWN: BigInt(1) << BigInt(28),
            SOCK_ACCEPT: BigInt(1) << BigInt(29)
        },
        m = {
            ATIM: 1,
            ATIM_NOW: 2,
            MTIM: 4,
            MTIM_NOW: 8
        },
        q = {
            SUBSCRIPTION_CLOCK_ABSTIME: 1
        },
        W = 64,
        z = 48,
        $ = 32;
    var M = (e => (e[e.CUR = 0] = "CUR", e[e.END = 1] = "END", e[e.SET = 2] = "SET", e))(M || {});
    class tt {
        constructor(t) {
            d(this, "fs");
            d(this, "nextFD", 10);
            d(this, "openMap", new Map);
            this.fs = {
                ...t
            }, this.openMap.set(3, new u(this.fs, "/"))
        }
        openFile(t, i, n) {
            const s = new I(t, n);
            i && (s.buffer = new Uint8Array(new ArrayBuffer(1024), 0, 0));
            const a = this.nextFD;
            return this.openMap.set(a, s), this.nextFD++, [r.SUCCESS, a]
        }
        openDir(t, i) {
            const n = new u(t, i),
                s = this.nextFD;
            return this.openMap.set(s, n), this.nextFD++, [r.SUCCESS, s]
        }
        hasDir(t, i) {
            return i === "." ? !0 : t.containsDirectory(i)
        }
        open(t, i, n, s) {
            const a = !!(n & N.CREAT),
                f = !!(n & N.DIRECTORY),
                c = !!(n & N.EXCL),
                o = !!(n & N.TRUNC),
                E = this.openMap.get(t);
            if (!(E instanceof u)) return [r.EBADF];
            if (E.containsFile(i)) return f ? [r.ENOTDIR] : c ? [r.EEXIST] : this.openFile(E.get(i), o, s);
            if (this.hasDir(E, i)) {
                if (i === ".") return this.openDir(this.fs, "/");
                const h = `/${i}/`,
                    S = Object.entries(this.fs).filter(([g]) => g.startsWith(h));
                return this.openDir(Object.fromEntries(S), h)
            } else {
                if (a) {
                    const h = E.fullPath(i);
                    return this.fs[h] = {
                        path: h,
                        mode: "binary",
                        content: new Uint8Array,
                        timestamps: {
                            access: new Date,
                            modification: new Date,
                            change: new Date
                        }
                    }, this.openFile(this.fs[h], o, s)
                }
                return [r.ENOENT]
            }
        }
        close(t) {
            if (!this.openMap.has(t)) return r.EBADF;
            const i = this.openMap.get(t);
            return i instanceof I && i.sync(), this.openMap.delete(t), r.SUCCESS
        }
        read(t, i) {
            const n = this.openMap.get(t);
            return !n || n instanceof u ? [r.EBADF] : [r.SUCCESS, n.read(i)]
        }
        pread(t, i, n) {
            const s = this.openMap.get(t);
            return !s || s instanceof u ? [r.EBADF] : [r.SUCCESS, s.pread(i, n)]
        }
        write(t, i) {
            const n = this.openMap.get(t);
            return !n || n instanceof u ? r.EBADF : (n.write(i), r.SUCCESS)
        }
        pwrite(t, i, n) {
            const s = this.openMap.get(t);
            return !s || s instanceof u ? r.EBADF : (s.pwrite(i, n), r.SUCCESS)
        }
        sync(t) {
            const i = this.openMap.get(t);
            return !i || i instanceof u ? r.EBADF : (i.sync(), r.SUCCESS)
        }
        seek(t, i, n) {
            const s = this.openMap.get(t);
            return !s || s instanceof u ? [r.EBADF] : [r.SUCCESS, s.seek(i, n)]
        }
        tell(t) {
            const i = this.openMap.get(t);
            return !i || i instanceof u ? [r.EBADF] : [r.SUCCESS, i.tell()]
        }
        renumber(t, i) {
            return !this.exists(t) || !this.exists(i) ? r.EBADF : (t === i || (this.close(i), this.openMap.set(i, this.openMap.get(t))), r.SUCCESS)
        }
        unlink(t, i) {
            const n = this.openMap.get(t);
            if (!(n instanceof u)) return r.EBADF;
            if (!n.contains(i)) return r.ENOENT;
            for (const s of Object.keys(this.fs))(s === n.fullPath(i) || s.startsWith(`${n.fullPath(i)}/`)) && delete this.fs[s];
            return r.SUCCESS
        }
        rename(t, i, n, s) {
            const a = this.openMap.get(t),
                f = this.openMap.get(n);
            if (!(a instanceof u) || !(f instanceof u)) return r.EBADF;
            if (!a.contains(i)) return r.ENOENT;
            if (f.contains(s)) return r.EEXIST;
            const c = a.fullPath(i),
                o = f.fullPath(s);
            for (const E of Object.keys(this.fs))
                if (E.startsWith(c)) {
                    const h = E.replace(c, o);
                    this.fs[h] = this.fs[E], this.fs[h].path = h, delete this.fs[E]
                } return r.SUCCESS
        }
        list(t) {
            const i = this.openMap.get(t);
            return i instanceof u ? [r.SUCCESS, i.list()] : [r.EBADF]
        }
        stat(t) {
            const i = this.openMap.get(t);
            return i instanceof I ? [r.SUCCESS, i.stat()] : [r.EBADF]
        }
        pathStat(t, i) {
            const n = this.openMap.get(t);
            if (!(n instanceof u)) return [r.EBADF];
            if (n.containsFile(i)) {
                const s = n.fullPath(i),
                    a = new I(this.fs[s], 0).stat();
                return [r.SUCCESS, a]
            } else if (this.hasDir(n, i)) {
                if (i === ".") return [r.SUCCESS, new u(this.fs, "/").stat()];
                const s = `/${i}/`,
                    a = Object.entries(this.fs).filter(([c]) => c.startsWith(s)),
                    f = new u(Object.fromEntries(a), s).stat();
                return [r.SUCCESS, f]
            } else return [r.ENOENT]
        }
        setFlags(t, i) {
            const n = this.openMap.get(t);
            return n instanceof I ? (n.setFlags(i), r.SUCCESS) : r.EBADF
        }
        setSize(t, i) {
            const n = this.openMap.get(t);
            return n instanceof I ? (n.setSize(Number(i)), r.SUCCESS) : r.EBADF
        }
        setAccessTime(t, i) {
            const n = this.openMap.get(t);
            return n instanceof I ? (n.setAccessTime(i), r.SUCCESS) : r.EBADF
        }
        setModificationTime(t, i) {
            const n = this.openMap.get(t);
            return n instanceof I ? (n.setModificationTime(i), r.SUCCESS) : r.EBADF
        }
        pathSetAccessTime(t, i, n) {
            const s = this.openMap.get(t);
            if (!(s instanceof u)) return r.EBADF;
            const a = s.get(i);
            if (!a) return r.ENOENT;
            const f = new I(a, 0);
            return f.setAccessTime(n), f.sync(), r.SUCCESS
        }
        pathSetModificationTime(t, i, n) {
            const s = this.openMap.get(t);
            if (!(s instanceof u)) return r.EBADF;
            const a = s.get(i);
            if (!a) return r.ENOENT;
            const f = new I(a, 0);
            return f.setModificationTime(n), f.sync(), r.SUCCESS
        }
        pathCreateDir(t, i) {
            const n = this.openMap.get(t);
            if (!(n instanceof u)) return r.EBADF;
            if (n.contains(i)) return r.ENOENT;
            const s = `${n.fullPath(i)}/.runno`;
            return this.fs[s] = {
                path: s,
                timestamps: {
                    access: new Date,
                    modification: new Date,
                    change: new Date
                },
                mode: "string",
                content: ""
            }, r.SUCCESS
        }
        exists(t) {
            return this.openMap.has(t)
        }
        fileType(t) {
            const i = this.openMap.get(t);
            return i ? i instanceof I ? A.REGULAR_FILE : A.DIRECTORY : A.UNKNOWN
        }
        fileFdflags(t) {
            const i = this.openMap.get(t);
            return i instanceof I ? i.fdflags : 0
        }
    }
    class I {
        constructor(t, i) {
            d(this, "file");
            d(this, "buffer");
            d(this, "_offset", BigInt(0));
            d(this, "isDirty", !1);
            d(this, "fdflags");
            d(this, "flagAppend");
            d(this, "flagDSync");
            d(this, "flagNonBlock");
            d(this, "flagRSync");
            d(this, "flagSync");
            if (this.file = t, this.file.mode === "string") {
                const n = new TextEncoder;
                this.buffer = n.encode(this.file.content)
            } else this.buffer = this.file.content;
            this.fdflags = i, this.flagAppend = !!(i & O.APPEND), this.flagDSync = !!(i & O.DSYNC), this.flagNonBlock = !!(i & O.NONBLOCK), this.flagRSync = !!(i & O.RSYNC), this.flagSync = !!(i & O.SYNC)
        }
        get offset() {
            return Number(this._offset)
        }
        read(t) {
            const i = this.buffer.subarray(this.offset, this.offset + t);
            return this._offset += BigInt(i.length), i
        }
        pread(t, i) {
            return this.buffer.subarray(i, i + t)
        }
        write(t) {
            if (this.isDirty = !0, this.flagAppend) {
                const i = this.buffer.length;
                this.resize(i + t.byteLength), this.buffer.set(t, i)
            } else {
                const i = Math.max(this.offset + t.byteLength, this.buffer.byteLength);
                this.resize(i), this.buffer.set(t, this.offset), this._offset += BigInt(t.byteLength)
            }(this.flagDSync || this.flagSync) && this.sync()
        }
        pwrite(t, i) {
            if (this.isDirty = !0, this.flagAppend) {
                const n = this.buffer.length;
                this.resize(n + t.byteLength), this.buffer.set(t, n)
            } else {
                const n = Math.max(i + t.byteLength, this.buffer.byteLength);
                this.resize(n), this.buffer.set(t, i)
            }(this.flagDSync || this.flagSync) && this.sync()
        }
        sync() {
            if (!this.isDirty) return;
            if (this.isDirty = !1, this.file.mode === "binary") {
                this.file.content = new Uint8Array(this.buffer);
                return
            }
            const t = new TextDecoder;
            this.file.content = t.decode(this.buffer)
        }
        seek(t, i) {
            switch (i) {
                case D.SET:
                    this._offset = t;
                    break;
                case D.CUR:
                    this._offset += t;
                    break;
                case D.END:
                    this._offset = BigInt(this.buffer.length) + t;
                    break
            }
            return this._offset
        }
        tell() {
            return this._offset
        }
        stat() {
            return {
                path: this.file.path,
                timestamps: this.file.timestamps,
                type: A.REGULAR_FILE,
                byteLength: this.buffer.length
            }
        }
        setFlags(t) {
            this.fdflags = t
        }
        setSize(t) {
            this.resize(t)
        }
        setAccessTime(t) {
            this.file.timestamps.access = t
        }
        setModificationTime(t) {
            this.file.timestamps.modification = t
        }
        resize(t) {
            if (t <= this.buffer.buffer.byteLength) {
                this.buffer = new Uint8Array(this.buffer.buffer, 0, t);
                return
            }
            let i;
            this.buffer.buffer.byteLength === 0 ? i = new ArrayBuffer(t < 1024 ? 1024 : t * 2) : t > this.buffer.buffer.byteLength * 2 ? i = new ArrayBuffer(t * 2) : i = new ArrayBuffer(this.buffer.buffer.byteLength * 2);
            const n = new Uint8Array(i, 0, t);
            n.set(this.buffer), this.buffer = n
        }
    }

    function P(e, t) {
        const i = t.replace(/[/\-\\^$*+?.()|[\]{}]/g, "\\$&"),
            n = new RegExp(`^${i}`);
        return e.replace(n, "")
    }
    class u {
        constructor(t, i) {
            d(this, "dir");
            d(this, "prefix");
            this.dir = t, this.prefix = i
        }
        containsFile(t) {
            for (const i of Object.keys(this.dir))
                if (P(i, this.prefix) === t) return !0;
            return !1
        }
        containsDirectory(t) {
            for (const i of Object.keys(this.dir))
                if (P(i, this.prefix).startsWith(`${t}/`)) return !0;
            return !1
        }
        contains(t) {
            for (const i of Object.keys(this.dir)) {
                const n = P(i, this.prefix);
                if (n === t || n.startsWith(`${t}/`)) return !0
            }
            return !1
        }
        get(t) {
            return this.dir[this.fullPath(t)]
        }
        fullPath(t) {
            return `${this.prefix}${t}`
        }
        list() {
            const t = [],
                i = new Set;
            for (const n of Object.keys(this.dir)) {
                const s = P(n, this.prefix);
                if (s.includes("/")) {
                    const a = s.split("/")[0];
                    if (i.has(a)) continue;
                    i.add(a), t.push({
                        name: a,
                        type: A.DIRECTORY
                    })
                } else t.push({
                    name: s,
                    type: A.REGULAR_FILE
                })
            }
            return t
        }
        stat() {
            return {
                path: this.prefix,
                timestamps: {
                    access: new Date,
                    modification: new Date,
                    change: new Date
                },
                type: A.DIRECTORY,
                byteLength: 0
            }
        }
    }
    let k = [];

    function U(e) {
        k.push(e)
    }

    function et() {
        const e = k;
        return k = [], e
    }
    class Y {
        constructor(t) {
            d(this, "instance");
            d(this, "module");
            d(this, "memory");
            d(this, "context");
            d(this, "drive");
            d(this, "initialized", !1);
            this.context = t, this.drive = new tt(t.fs)
        }
        static async start(t, i) {
            const n = new Y(i),
                s = await WebAssembly.instantiateStreaming(t, {
                    wasi_snapshot_preview1: n.getImports("preview1", i.debug),
                    wasi_unstable: n.getImports("unstable", i.debug)
                });
            return n.init(s), n.start()
        }
        init(t) {
            this.instance = t.instance, this.module = t.module, this.memory = this.instance.exports.memory, this.initialized = !0
        }
        start() {
            if (!this.initialized) throw new Error("WASI must be initialized with init(wasm) first");
            let membufview = new Uint32Array(this.memory.buffer);
            membufview.fill(0xdeadbeef, -16384); // 64 kB (divided by 4)
            const t = this.instance.exports._start;
            const sa = () => this.instance.exports.trainer_alloc_calls ? this.instance.exports.trainer_alloc_calls() : 0
            const sf = () => this.instance.exports.trainer_free_calls ? this.instance.exports.trainer_free_calls() : 0

            const readStr = (pos) => {
                let readPos = pos
                let memview = new Uint8Array(this.memory.buffer)
                let charlimit = 8192
                while ( --charlimit > 0 ) {
                    const chr = memview[readPos++]
                    if (chr === 0) break;
                }
                return new TextDecoder().decode(memview.subarray(pos, readPos))
            }

            const stp = () => this.instance.exports.trainer_tests_passed ? this.instance.exports.trainer_tests_passed() : 0
            const stt = () => this.instance.exports.trainer_tests_total ? this.instance.exports.trainer_tests_total() : 0

            const sti = () => this.instance.exports.trainer_input ? readStr(this.instance.exports.trainer_input()) : ""
            const sto = () => this.instance.exports.trainer_output ? readStr(this.instance.exports.trainer_output()) : ""
            const str = () => this.instance.exports.trainer_ref ? readStr(this.instance.exports.trainer_ref()) : ""
            try {
                t()
            } catch (i) {
                if (i instanceof X) return {
                    exitCode: i.code,
                    allocCalls: sa(), freeCalls: sf(),
                    testsPassed: stp(), testsTotal: stt(),
                    input: sti(), output: sto(), ref: str(),
                    fs: this.drive.fs
                };
                if (i instanceof WebAssembly.RuntimeError) return {
                    exitCode: 134,
                    allocCalls: sa(), freeCalls: sf(),
                    testsPassed: stp(), testsTotal: stt(),
                    input: sti(), output: sto(), ref: str(),
                    fs: this.drive.fs
                };
                throw i
            }
            return {
                exitCode: 0,
                allocCalls: sa(), freeCalls: sf(),
                testsPassed: stp(), testsTotal: stt(),
                input: sti(), output: sto(), ref: str(),
                fs: this.drive.fs
            }
        }
        getImports(t, i) {
            const n = {
                args_get: this.args_get.bind(this),
                args_sizes_get: this.args_sizes_get.bind(this),
                clock_res_get: this.clock_res_get.bind(this),
                clock_time_get: this.clock_time_get.bind(this),
                environ_get: this.environ_get.bind(this),
                environ_sizes_get: this.environ_sizes_get.bind(this),
                proc_exit: this.proc_exit.bind(this),
                random_get: this.random_get.bind(this),
                sched_yield: this.sched_yield.bind(this),
                fd_advise: this.fd_advise.bind(this),
                fd_allocate: this.fd_allocate.bind(this),
                fd_close: this.fd_close.bind(this),
                fd_datasync: this.fd_datasync.bind(this),
                fd_fdstat_get: this.fd_fdstat_get.bind(this),
                fd_fdstat_set_flags: this.fd_fdstat_set_flags.bind(this),
                fd_fdstat_set_rights: this.fd_fdstat_set_rights.bind(this),
                fd_filestat_get: this.fd_filestat_get.bind(this),
                fd_filestat_set_size: this.fd_filestat_set_size.bind(this),
                fd_filestat_set_times: this.fd_filestat_set_times.bind(this),
                fd_pread: this.fd_pread.bind(this),
                fd_prestat_dir_name: this.fd_prestat_dir_name.bind(this),
                fd_prestat_get: this.fd_prestat_get.bind(this),
                fd_pwrite: this.fd_pwrite.bind(this),
                fd_read: this.fd_read.bind(this),
                fd_readdir: this.fd_readdir.bind(this),
                fd_renumber: this.fd_renumber.bind(this),
                fd_seek: this.fd_seek.bind(this),
                fd_sync: this.fd_sync.bind(this),
                fd_tell: this.fd_tell.bind(this),
                fd_write: this.fd_write.bind(this),
                path_filestat_get: this.path_filestat_get.bind(this),
                path_filestat_set_times: this.path_filestat_set_times.bind(this),
                path_open: this.path_open.bind(this),
                path_rename: this.path_rename.bind(this),
                path_unlink_file: this.path_unlink_file.bind(this),
                path_create_directory: this.path_create_directory.bind(this),
                path_link: this.path_link.bind(this),
                path_readlink: this.path_readlink.bind(this),
                path_remove_directory: this.path_remove_directory.bind(this),
                path_symlink: this.path_symlink.bind(this),
                poll_oneoff: this.poll_oneoff.bind(this),
                proc_raise: this.proc_raise.bind(this),
                sock_accept: this.sock_accept.bind(this),
                sock_recv: this.sock_recv.bind(this),
                sock_send: this.sock_send.bind(this),
                sock_shutdown: this.sock_shutdown.bind(this),
                sock_open: this.sock_open.bind(this),
                sock_listen: this.sock_listen.bind(this),
                sock_connect: this.sock_connect.bind(this),
                sock_setsockopt: this.sock_setsockopt.bind(this),
                sock_bind: this.sock_bind.bind(this),
                sock_getlocaladdr: this.sock_getlocaladdr.bind(this),
                sock_getpeeraddr: this.sock_getpeeraddr.bind(this),
                sock_getaddrinfo: this.sock_getaddrinfo.bind(this)
            };
            t === "unstable" && (n.path_filestat_get = this.unstable_path_filestat_get.bind(this), n.fd_filestat_get = this.unstable_fd_filestat_get.bind(this), n.fd_seek = this.unstable_fd_seek.bind(this));
            for (const [s, a] of Object.entries(n)) n[s] = function() {
                let f = a.apply(this, arguments);
                if (i) {
                    const c = et();
                    f = i(s, [...arguments], f, c) ?? f
                }
                return f
            };
            return n
        }
        get envArray() {
            return Object.entries(this.context.env).map(([t, i]) => `${t}=${i}`)
        }
        args_get(t, i) {
            const n = new DataView(this.memory.buffer);
            for (const s of this.context.args) {
                n.setUint32(t, i, !0), t += 4;
                const a = new TextEncoder().encode(`${s}\0`);
                new Uint8Array(this.memory.buffer, i, a.byteLength).set(a), i += a.byteLength
            }
            return r.SUCCESS
        }
        args_sizes_get(t, i) {
            const n = this.context.args,
                s = n.reduce((f, c) => f + new TextEncoder().encode(`${c}\0`).byteLength, 0),
                a = new DataView(this.memory.buffer);
            return a.setUint32(t, n.length, !0), a.setUint32(i, s, !0), r.SUCCESS
        }
        clock_res_get(t, i) {
            switch (t) {
                case T.REALTIME:
                case T.MONOTONIC:
                case T.PROCESS_CPUTIME_ID:
                case T.THREAD_CPUTIME_ID:
                    return new DataView(this.memory.buffer).setBigUint64(i, BigInt(1e6), !0), r.SUCCESS
            }
            return r.EINVAL
        }
        clock_time_get(t, i, n) {
            switch (t) {
                case T.REALTIME:
                case T.MONOTONIC:
                case T.PROCESS_CPUTIME_ID:
                case T.THREAD_CPUTIME_ID:
                    return new DataView(this.memory.buffer).setBigUint64(n, y(new Date), !0), r.SUCCESS
            }
            return r.EINVAL
        }
        environ_get(t, i) {
            const n = new DataView(this.memory.buffer);
            for (const s of this.envArray) {
                n.setUint32(t, i, !0), t += 4;
                const a = new TextEncoder().encode(`${s}\0`);
                new Uint8Array(this.memory.buffer, i, a.byteLength).set(a), i += a.byteLength
            }
            return r.SUCCESS
        }
        environ_sizes_get(t, i) {
            const n = this.envArray.reduce((a, f) => a + new TextEncoder().encode(`${f}\0`).byteLength, 0),
                s = new DataView(this.memory.buffer);
            return s.setUint32(t, this.envArray.length, !0), s.setUint32(i, n, !0), r.SUCCESS
        }
        proc_exit(t) {
            throw new X(t)
        }
        random_get(t, i) {
            const n = new Uint8Array(this.memory.buffer, t, i);
            return crypto.getRandomValues(n), r.SUCCESS
        }
        sched_yield() {
            return r.SUCCESS
        }
        fd_read(t, i, n, s) {
            if (t === 1 || t === 2) return r.ENOTSUP;
            const a = new DataView(this.memory.buffer),
                f = v(a, i, n),
                c = new TextEncoder;
            let o = 0,
                E = r.SUCCESS;
            for (const h of f) {
                let S;
                if (t === 0) {
                    const C = this.context.stdin(h.byteLength);
                    if (!C) break;
                    S = c.encode(C)
                } else {
                    const [C, l] = this.drive.read(t, h.byteLength);
                    if (C) {
                        E = C;
                        break
                    } else S = l
                }
                const g = Math.min(h.byteLength, S.byteLength);
                h.set(S.subarray(0, g)), o += g
            }
            return U({
                bytesRead: o
            }), a.setUint32(s, o, !0), E
        }
        fd_write(t, i, n, s) {
            if (t === 0) return r.ENOTSUP;
            const a = new DataView(this.memory.buffer),
                f = v(a, i, n),
                c = new TextDecoder;
            let o = 0,
                E = r.SUCCESS;
            for (const h of f)
                if (h.byteLength !== 0) {
                    if (t === 1 || t === 2) {
                        const S = t === 1 ? this.context.stdout : this.context.stderr,
                            g = c.decode(h);
                        S(g), U({
                            output: g
                        })
                    } else if (E = this.drive.write(t, h), E != r.SUCCESS) break;
                    o += h.byteLength
                } return a.setUint32(s, o, !0), E
        }
        fd_advise() {
            return r.SUCCESS
        }
        fd_allocate(t, i, n) {
            return this.drive.pwrite(t, new Uint8Array(Number(n)), Number(i))
        }
        fd_close(t) {
            return this.drive.close(t)
        }
        fd_datasync(t) {
            return this.drive.sync(t)
        }
        fd_fdstat_get(t, i) {
            if (t < 3) {
                let c;
                if (this.context.isTTY) {
                    const E = x ^ _.FD_SEEK ^ _.FD_TELL;
                    c = V(A.CHARACTER_DEVICE, 0, E)
                } else c = V(A.CHARACTER_DEVICE, 0);
                return new Uint8Array(this.memory.buffer, i, c.byteLength).set(c), r.SUCCESS
            }
            if (!this.drive.exists(t)) return r.EBADF;
            const n = this.drive.fileType(t),
                s = this.drive.fileFdflags(t),
                a = V(n, s);
            return new Uint8Array(this.memory.buffer, i, a.byteLength).set(a), r.SUCCESS
        }
        fd_fdstat_set_flags(t, i) {
            return this.drive.setFlags(t, i)
        }
        fd_fdstat_set_rights() {
            return r.SUCCESS
        }
        fd_filestat_get(t, i) {
            return this.shared_fd_filestat_get(t, i, "preview1")
        }
        unstable_fd_filestat_get(t, i) {
            return this.shared_fd_filestat_get(t, i, "unstable")
        }
        shared_fd_filestat_get(t, i, n) {
            const s = n === "unstable" ? Z : j;
            if (t < 3) {
                let E;
                switch (t) {
                    case 0:
                        E = "/dev/stdin";
                        break;
                    case 1:
                        E = "/dev/stdout";
                        break;
                    case 2:
                        E = "/dev/stderr";
                        break;
                    default:
                        E = "/dev/undefined";
                        break
                }
                const h = s({
                    path: E,
                    byteLength: 0,
                    timestamps: {
                        access: new Date,
                        modification: new Date,
                        change: new Date
                    },
                    type: A.CHARACTER_DEVICE
                });
                return new Uint8Array(this.memory.buffer, i, h.byteLength).set(h), r.SUCCESS
            }
            const [a, f] = this.drive.stat(t);
            if (a != r.SUCCESS) return a;
            U({
                resolvedPath: f.path,
                stat: f
            });
            const c = s(f);
            return new Uint8Array(this.memory.buffer, i, c.byteLength).set(c), r.SUCCESS
        }
        fd_filestat_set_size(t, i) {
            return this.drive.setSize(t, i)
        }
        fd_filestat_set_times(t, i, n, s) {
            let a = null;
            s & m.ATIM && (a = p(i)), s & m.ATIM_NOW && (a = new Date);
            let f = null;
            if (s & m.MTIM && (f = p(n)), s & m.MTIM_NOW && (f = new Date), a) {
                const c = this.drive.setAccessTime(t, a);
                if (c != r.SUCCESS) return c
            }
            if (f) {
                const c = this.drive.setModificationTime(t, f);
                if (c != r.SUCCESS) return c
            }
            return r.SUCCESS
        }
        fd_pread(t, i, n, s, a) {
            if (t === 1 || t === 2) return r.ENOTSUP;
            if (t === 0) return this.fd_read(t, i, n, a);
            const f = new DataView(this.memory.buffer),
                c = v(f, i, n);
            let o = 0,
                E = r.SUCCESS;
            for (const h of c) {
                const [S, g] = this.drive.pread(t, h.byteLength, Number(s) + o);
                if (S !== r.SUCCESS) {
                    E = S;
                    break
                }
                const C = Math.min(h.byteLength, g.byteLength);
                h.set(g.subarray(0, C)), o += C
            }
            return f.setUint32(a, o, !0), E
        }
        fd_prestat_dir_name(t, i, n) {
            if (t !== 3) return r.EBADF;
            const s = new TextEncoder().encode("/");
            return new Uint8Array(this.memory.buffer, i, n).set(s.subarray(0, n)), r.SUCCESS
        }
        fd_prestat_get(t, i) {
            if (t !== 3) return r.EBADF;
            const n = new TextEncoder().encode("."),
                s = new DataView(this.memory.buffer, i);
            return s.setUint8(0, G.DIR), s.setUint32(4, n.byteLength, !0), r.SUCCESS
        }
        fd_pwrite(t, i, n, s, a) {
            if (t === 0) return r.ENOTSUP;
            if (t === 1 || t === 2) return this.fd_write(t, i, n, a);
            const f = new DataView(this.memory.buffer),
                c = v(f, i, n);
            let o = 0,
                E = r.SUCCESS;
            for (const h of c)
                if (h.byteLength !== 0) {
                    if (E = this.drive.pwrite(t, h, Number(s)), E != r.SUCCESS) break;
                    o += h.byteLength
                } return f.setUint32(a, o, !0), E
        }
        fd_readdir(t, i, n, s, a) {
            const [f, c] = this.drive.list(t);
            if (f != r.SUCCESS) return f;
            let o = [],
                E = 0;
            for (const {
                name: w,
                type: F
            }
                of c) {
                const K = nt(w, F, E);
                o.push(K), E++
            }
            o = o.slice(Number(s));
            const h = o.reduce((w, F) => w + F.byteLength, 0),
                S = new Uint8Array(h);
            let g = 0;
            for (const w of o) S.set(w, g), g += w.byteLength;
            const C = new Uint8Array(this.memory.buffer, i, n),
                l = S.subarray(0, n);
            return C.set(l), new DataView(this.memory.buffer).setUint32(a, l.byteLength, !0), r.SUCCESS
        }
        fd_renumber(t, i) {
            return this.drive.renumber(t, i)
        }
        fd_seek(t, i, n, s) {
            const [a, f] = this.drive.seek(t, i, n);
            return a !== r.SUCCESS || (U({
                newOffset: f.toString()
            }), new DataView(this.memory.buffer).setBigUint64(s, f, !0)), a
        }
        unstable_fd_seek(t, i, n, s) {
            const a = st[n];
            return this.fd_seek(t, i, a, s)
        }
        fd_sync(t) {
            return this.drive.sync(t)
        }
        fd_tell(t, i) {
            const [n, s] = this.drive.tell(t);
            return n !== r.SUCCESS || new DataView(this.memory.buffer).setBigUint64(i, s, !0), n
        }
        path_filestat_get(t, i, n, s, a) {
            return this.shared_path_filestat_get(t, i, n, s, a, "preview1")
        }
        unstable_path_filestat_get(t, i, n, s, a) {
            return this.shared_path_filestat_get(t, i, n, s, a, "unstable")
        }
        shared_path_filestat_get(t, i, n, s, a, f) {
            const c = f === "unstable" ? Z : j,
                o = new TextDecoder().decode(new Uint8Array(this.memory.buffer, n, s));
            U({
                path: o
            });
            const [E, h] = this.drive.pathStat(t, o);
            if (E != r.SUCCESS) return E;
            const S = c(h);
            return new Uint8Array(this.memory.buffer, a, S.byteLength).set(S), E
        }
        path_filestat_set_times(t, i, n, s, a, f, c) {
            let o = null;
            c & m.ATIM && (o = p(a)), c & m.ATIM_NOW && (o = new Date);
            let E = null;
            c & m.MTIM && (E = p(f)), c & m.MTIM_NOW && (E = new Date);
            const h = new TextDecoder().decode(new Uint8Array(this.memory.buffer, n, s));
            if (o) {
                const S = this.drive.pathSetAccessTime(t, h, o);
                if (S != r.SUCCESS) return S
            }
            if (E) {
                const S = this.drive.pathSetModificationTime(t, h, E);
                if (S != r.SUCCESS) return S
            }
            return r.SUCCESS
        }
        path_open(t, i, n, s, a, f, c, o, E) {
            const h = new DataView(this.memory.buffer),
                S = B(this.memory, n, s),
                g = !!(a & N.CREAT),
                C = !!(a & N.DIRECTORY),
                l = !!(a & N.EXCL),
                J = !!(a & N.TRUNC),
                w = !!(o & O.APPEND),
                F = !!(o & O.DSYNC),
                K = !!(o & O.NONBLOCK),
                St = !!(o & O.RSYNC),
                _t = !!(o & O.SYNC);
            U({
                path: S,
                openFlags: {
                    createFileIfNone: g,
                    failIfNotDir: C,
                    failIfFileExists: l,
                    truncateFile: J
                },
                fileDescriptorFlags: {
                    flagAppend: w,
                    flagDSync: F,
                    flagNonBlock: K,
                    flagRSync: St,
                    flagSync: _t
                }
            });
            const [R, dt] = this.drive.open(t, S, a, o);
            return R || (h.setUint32(E, dt, !0), R)
        }
        path_rename(t, i, n, s, a, f) {
            const c = B(this.memory, i, n),
                o = B(this.memory, a, f);
            return U({
                oldPath: c,
                newPath: o
            }), this.drive.rename(t, c, s, o)
        }
        path_unlink_file(t, i, n) {
            const s = B(this.memory, i, n);
            return U({
                path: s
            }), this.drive.unlink(t, s)
        }
        poll_oneoff(t, i, n, s) {
            for (let f = 0; f < n; f++) {
                const c = new Uint8Array(this.memory.buffer, t + f * z, z),
                    o = it(c),
                    E = new Uint8Array(this.memory.buffer, i + f * $, $);
                let h = 0,
                    S = r.SUCCESS;
                switch (o.type) {
                    case b.CLOCK:
                        for (; new Date < o.timeout;);
                        E.set(rt(o.userdata, r.SUCCESS));
                        break;
                    case b.FD_READ:
                        if (o.fd < 3) o.fd === 0 ? (S = r.SUCCESS, h = 32) : S = r.EBADF;
                        else {
                            const [g, C] = this.drive.stat(o.fd);
                            S = g, h = C ? C.byteLength : 0
                        }
                        E.set(Q(o.userdata, S, b.FD_READ, BigInt(h)));
                        break;
                    case b.FD_WRITE:
                        if (h = 0, S = r.SUCCESS, o.fd < 3) o.fd === 0 ? S = r.EBADF : (S = r.SUCCESS, h = 1024);
                        else {
                            const [g, C] = this.drive.stat(o.fd);
                            S = g, h = C ? C.byteLength : 0
                        }
                        E.set(Q(o.userdata, S, b.FD_READ, BigInt(h)));
                        break
                }
            }
            return new DataView(this.memory.buffer, s, 4).setUint32(0, n, !0), r.SUCCESS
        }
        path_create_directory(t, i, n) {
            const s = B(this.memory, i, n);
            return this.drive.pathCreateDir(t, s)
        }
        path_link() {
            return r.ENOSYS
        }
        path_readlink() {
            return r.ENOSYS
        }
        path_remove_directory() {
            return r.ENOSYS
        }
        path_symlink() {
            return r.ENOSYS
        }
        proc_raise() {
            return r.ENOSYS
        }
        sock_accept() {
            return r.ENOSYS
        }
        sock_recv() {
            return r.ENOSYS
        }
        sock_send() {
            return r.ENOSYS
        }
        sock_shutdown() {
            return r.ENOSYS
        }
        sock_open() {
            return r.ENOSYS
        }
        sock_listen() {
            return r.ENOSYS
        }
        sock_connect() {
            return r.ENOSYS
        }
        sock_setsockopt() {
            return r.ENOSYS
        }
        sock_bind() {
            return r.ENOSYS
        }
        sock_getlocaladdr() {
            return r.ENOSYS
        }
        sock_getpeeraddr() {
            return r.ENOSYS
        }
        sock_getaddrinfo() {
            return r.ENOSYS
        }
    }
    const x = _.FD_DATASYNC | _.FD_READ | _.FD_SEEK | _.FD_FDSTAT_SET_FLAGS | _.FD_SYNC | _.FD_TELL | _.FD_WRITE | _.FD_ADVISE | _.FD_ALLOCATE | _.PATH_CREATE_DIRECTORY | _.PATH_CREATE_FILE | _.PATH_LINK_SOURCE | _.PATH_LINK_TARGET | _.PATH_OPEN | _.FD_READDIR | _.PATH_READLINK | _.PATH_RENAME_SOURCE | _.PATH_RENAME_TARGET | _.PATH_FILESTAT_GET | _.PATH_FILESTAT_SET_SIZE | _.PATH_FILESTAT_SET_TIMES | _.FD_FILESTAT_GET | _.FD_FILESTAT_SET_SIZE | _.FD_FILESTAT_SET_TIMES | _.PATH_SYMLINK | _.PATH_REMOVE_DIRECTORY | _.PATH_UNLINK_FILE | _.POLL_FD_READWRITE | _.SOCK_SHUTDOWN | _.SOCK_ACCEPT;
    class X extends Error {
        constructor(i) {
            super();
            d(this, "code");
            this.code = i
        }
    }

    function B(e, t, i) {
        return new TextDecoder().decode(new Uint8Array(e.buffer, t, i))
    }

    function v(e, t, i) {
        let n = Array(i);
        for (let s = 0; s < i; s++) {
            const a = e.getUint32(t, !0);
            t += 4;
            const f = e.getUint32(t, !0);
            t += 4, n[s] = new Uint8Array(e.buffer, a, f)
        }
        return n
    }

    function it(e) {
        const t = new Uint8Array(8);
        t.set(e.subarray(0, 8));
        const i = e[8],
            n = new DataView(e.buffer, e.byteOffset + 9);
        switch (i) {
            case b.FD_READ:
            case b.FD_WRITE:
                return {
                    userdata: t, type: i, fd: n.getUint32(0, !0)
                };
            case b.CLOCK:
                const s = n.getUint16(24, !0),
                    a = y(new Date),
                    f = n.getBigUint64(8, !0),
                    c = n.getBigUint64(16, !0),
                    o = s & q.SUBSCRIPTION_CLOCK_ABSTIME ? f : a + f;
                return {
                    userdata: t, type: i, id: n.getUint32(0, !0), timeout: p(o), precision: p(o + c)
                }
        }
    }

    function j(e) {
        const t = new Uint8Array(W),
            i = new DataView(t.buffer);
        return i.setBigUint64(0, BigInt(0), !0), i.setBigUint64(8, BigInt(H(e.path)), !0), i.setUint8(16, e.type), i.setBigUint64(24, BigInt(1), !0), i.setBigUint64(32, BigInt(e.byteLength), !0), i.setBigUint64(40, y(e.timestamps.access), !0), i.setBigUint64(48, y(e.timestamps.modification), !0), i.setBigUint64(56, y(e.timestamps.change), !0), t
    }

    function Z(e) {
        const t = new Uint8Array(W),
            i = new DataView(t.buffer);
        return i.setBigUint64(0, BigInt(0), !0), i.setBigUint64(8, BigInt(H(e.path)), !0), i.setUint8(16, e.type), i.setUint32(20, 1, !0), i.setBigUint64(24, BigInt(e.byteLength), !0), i.setBigUint64(32, y(e.timestamps.access), !0), i.setBigUint64(40, y(e.timestamps.modification), !0), i.setBigUint64(48, y(e.timestamps.change), !0), t
    }

    function V(e, t, i) {
        const n = i ?? x,
            s = i ?? x,
            a = new Uint8Array(24),
            f = new DataView(a.buffer, 0, 24);
        return f.setUint8(0, e), f.setUint32(2, t, !0), f.setBigUint64(8, n, !0), f.setBigUint64(16, s, !0), a
    }

    function nt(e, t, i) {
        const n = new TextEncoder().encode(e),
            s = 24 + n.byteLength,
            a = new Uint8Array(s),
            f = new DataView(a.buffer);
        return f.setBigUint64(0, BigInt(i + 1), !0), f.setBigUint64(8, BigInt(H(e)), !0), f.setUint32(16, n.length, !0), f.setUint8(20, t), a.set(n, 24), a
    }

    function rt(e, t) {
        const i = new Uint8Array(32);
        i.set(e, 0);
        const n = new DataView(i.buffer);
        return n.setUint16(8, t, !0), n.setUint16(10, b.CLOCK, !0), i
    }

    function Q(e, t, i, n) {
        const s = new Uint8Array(32);
        s.set(e, 0);
        const a = new DataView(s.buffer);
        return a.setUint16(8, t, !0), a.setUint16(10, i, !0), a.setBigUint64(16, n, !0), s
    }

    function H(e, t = 0) {
        let i = 3735928559 ^ t,
            n = 1103547991 ^ t;
        for (let s = 0, a; s < e.length; s++) a = e.charCodeAt(s), i = Math.imul(i ^ a, 2654435761), n = Math.imul(n ^ a, 1597334677);
        return i = Math.imul(i ^ i >>> 16, 2246822507) ^ Math.imul(n ^ n >>> 13, 3266489909), n = Math.imul(n ^ n >>> 16, 2246822507) ^ Math.imul(i ^ i >>> 13, 3266489909), 4294967296 * (2097151 & n) + (i >>> 0)
    }

    function y(e) {
        return BigInt(e.getTime()) * BigInt(1e6)
    }

    function p(e) {
        return new Date(Number(e / BigInt(1e6)))
    }
    const st = {
        [M.CUR]: D.CUR,
        [M.END]: D.END,
        [M.SET]: D.SET
    };
    class at {
        constructor(t) {
            d(this, "fs");
            d(this, "args");
            d(this, "env");
            d(this, "stdin");
            d(this, "stdout");
            d(this, "stderr");
            d(this, "debug");
            d(this, "isTTY");
            this.fs = (t == null ? void 0 : t.fs) ?? {}, this.args = (t == null ? void 0 : t.args) ?? [], this.env = (t == null ? void 0 : t.env) ?? {}, this.stdin = (t == null ? void 0 : t.stdin) ?? (() => null), this.stdout = (t == null ? void 0 : t.stdout) ?? (() => {}), this.stderr = (t == null ? void 0 : t.stderr) ?? (() => {}), this.debug = t == null ? void 0 : t.debug, this.isTTY = !!(t != null && t.isTTY)
        }
    }
    onmessage = async e => {
        const t = e.data;
        switch (t.type) {
            case "startURL":
                try {
                    const i = await ftURL(t.binaryURL, t.stdinBuffer, t);
                    L({
                        target: "host",
                        type: "result",
                        result: i
                    })
                } catch (i) {
                    let n;
                    i instanceof Error ? n = {
                        message: i.message,
                        type: i.constructor.name
                    } : n = {
                        message: `unknown error - ${i}`,
                        type: "Unknown"
                    }, L({
                        target: "host",
                        type: "crash",
                        error: n
                    })
                }
                break
            case "startBuffer":
                try {
                    const i = await ftBuffer(t.binaryBuffer, t.stdinBuffer, t);
                    L({
                        target: "host",
                        type: "result",
                        result: i
                    })
                } catch (i) {
                    let n;
                    i instanceof Error ? n = {
                        message: i.message,
                        type: i.constructor.name
                    } : n = {
                        message: `unknown error - ${i}`,
                        type: "Unknown"
                    }, L({
                        target: "host",
                        type: "crash",
                        error: n
                    })
                }
                break
        }
    };

    function L(e) {
        postMessage(e)
    }
    async function ftBuffer(e, t, i) {
        let wasi = new Y(new at({
            ...i,
            stdout: ot,
            stderr: ct,
            stdin: n => ht(n, t),
            debug: Et
        }))
        wasi.init(await WebAssembly.instantiate(e, {
            wasi_snapshot_preview1: wasi.getImports("preview1"),
            wasi_unstable: wasi.getImports("unstable")
        }))
        return wasi.start()
    }
    async function ftURL(e, t, i) {
        return Y.start(fetch(e), new at({ ...i,
            stdout: ot,
            stderr: ct,
            stdin: n => ht(n, t),
            debug: Et
        }))
    }

    function ot(e) {
        L({
            target: "host",
            type: "stdout",
            text: e
        })
    }

    function ct(e) {
        L({
            target: "host",
            type: "stderr",
            text: e
        })
    }

    function Et(e, t, i, n) {
        return n = JSON.parse(JSON.stringify(n)), L({
            target: "host",
            type: "debug",
            name: e,
            args: t,
            ret: i,
            data: n
        }), i
    }

    function ht(e, t) {
        Atomics.wait(new Int32Array(t), 0, 0);
        const i = new DataView(t),
            n = i.getInt32(0);
        if (n < 0) return i.setInt32(0, 0), null;
        const s = new Uint8Array(t, 4, n),
            a = new TextDecoder().decode(s.slice(0, e)),
            f = s.slice(e, s.length);
        return i.setInt32(0, f.byteLength), s.set(f), a
    }
})();