if(!self.define){let s,l={};const e=(e,n)=>(e=new URL(e+".js",n).href,l[e]||new Promise((l=>{if("document"in self){const s=document.createElement("script");s.src=e,s.onload=l,document.head.appendChild(s)}else s=e,importScripts(e),l()})).then((()=>{let s=l[e];if(!s)throw new Error(`Module ${e} didn’t register its module`);return s})));self.define=(n,i)=>{const r=s||("document"in self?document.currentScript.src:"")||location.href;if(l[r])return;let u={};const o=s=>e(s,r),c={module:{uri:r},exports:u,require:o};l[r]=Promise.all(n.map((s=>c[s]||o(s)))).then((s=>(i(...s),u)))}}define(["./workbox-db5fc017"],(function(s){"use strict";s.setCacheNameDetails({prefix:"signature_web"}),self.addEventListener("message",(s=>{s.data&&"SKIP_WAITING"===s.data.type&&self.skipWaiting()})),s.precacheAndRoute([{url:"/css/153.4b25e054.css",revision:null},{url:"/css/235.e122cc6b.css",revision:null},{url:"/css/394.3c2c83e3.css",revision:null},{url:"/css/421.ac0fee6b.css",revision:null},{url:"/css/440.1cc82f9a.css",revision:null},{url:"/css/59.ff1f876a.css",revision:null},{url:"/css/720.ff1f876a.css",revision:null},{url:"/css/905.e2e2b97d.css",revision:null},{url:"/css/app.d1c04f89.css",revision:null},{url:"/css/chunk-vendors.a5d41a56.css",revision:null},{url:"/img/ZSSL.79ebb819.png",revision:null},{url:"/img/b1.11e0a2b9.png",revision:null},{url:"/img/bg.4cd5c5ea.jpg",revision:null},{url:"/img/jc_bannerbg.a52d6739.jpg",revision:null},{url:"/img/ldap.fc2cc4a5.png",revision:null},{url:"/img/user.c4a72329.png",revision:null},{url:"/img/wzlogo.3e109d33.png",revision:null},{url:"/index.html",revision:"0fa5a78a16f9f8131f30141b64a76521"},{url:"/js/145.30b68a2e.js",revision:null},{url:"/js/153.9df52636.js",revision:null},{url:"/js/235.2b2ad6b4.js",revision:null},{url:"/js/379.037ae664.js",revision:null},{url:"/js/394.2e0839f0.js",revision:null},{url:"/js/418.a33ce76f.js",revision:null},{url:"/js/420.52d11bed.js",revision:null},{url:"/js/421.c1e433ff.js",revision:null},{url:"/js/440.d3baae44.js",revision:null},{url:"/js/512.2d0cf984.js",revision:null},{url:"/js/520.c26389da.js",revision:null},{url:"/js/59.5e01c091.js",revision:null},{url:"/js/720.edc6a271.js",revision:null},{url:"/js/905.e5767638.js",revision:null},{url:"/js/app.768c807a.js",revision:null},{url:"/js/chunk-vendors.f4c63868.js",revision:null},{url:"/logo.png",revision:"4a44830d4e6f75cca23f521e41f5239e"},{url:"/manifest.json",revision:"b931e7ab842b4a3d6aecdd5ed858abad"},{url:"/robots.txt",revision:"735ab4f94fbcd57074377afca324c813"}],{})}));
