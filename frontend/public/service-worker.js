// give your cache a name
const cacheName = 'trainer-c39a21023b2-cache';

// put the static assets and routes you want to cache here
const filesToCache = [
    '/clang-15.2.wasm',
    '/wasm-ld.wasm',
    '/sysroot.3.tar',
];

// the event handler for the activate event
self.addEventListener('activate', e => self.clients.claim());

// the event handler for the installation event
// typically used to cache assets
self.addEventListener('install', e => {
    e.waitUntil(
        caches.open(cacheName)
         .then(cache => cache.addAll(filesToCache))
    );
});

// the fetch event handler, to intercept requests and serve all
// static assets from the cache
self.addEventListener('fetch', e => {
    e.respondWith(
        caches.match(e.request)
            .then(response => response ? response : fetch(e.request))
    )
});