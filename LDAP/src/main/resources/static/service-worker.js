if(!self.define){let s,l={};const e=(e,n)=>(e=new URL(e+".js",n).href,l[e]||new Promise((l=>{if("document"in self){const s=document.createElement("script");s.src=e,s.onload=l,document.head.appendChild(s)}else s=e,importScripts(e),l()})).then((()=>{let s=l[e];if(!s)throw new Error(`Module ${e} didn’t register its module`);return s})));self.define=(n,i)=>{const r=s||("document"in self?document.currentScript.src:"")||location.href;if(l[r])return;let u={};const c=s=>e(s,r),o={module:{uri:r},exports:u,require:c};l[r]=Promise.all(n.map((s=>o[s]||c(s)))).then((s=>(i(...s),u)))}}define(["./workbox-db5fc017"],(function(s){"use strict";s.setCacheNameDetails({prefix:"signature_web"}),self.addEventListener("message",(s=>{s.data&&"SKIP_WAITING"===s.data.type&&self.skipWaiting()})),s.precacheAndRoute([{url:"/css/153.4b25e054.css",revision:null},{url:"/css/298.e122cc6b.css",revision:null},{url:"/css/348.3c2c83e3.css",revision:null},{url:"/css/647.ff1f876a.css",revision:null},{url:"/css/668.ac0fee6b.css",revision:null},{url:"/css/720.ff1f876a.css",revision:null},{url:"/css/781.785dfe66.css",revision:null},{url:"/css/905.e2e2b97d.css",revision:null},{url:"/css/app.669bfa47.css",revision:null},{url:"/css/chunk-vendors.a5d41a56.css",revision:null},{url:"/img/ZSSL.79ebb819.png",revision:null},{url:"/img/b1.11e0a2b9.png",revision:null},{url:"/img/bg.4cd5c5ea.jpg",revision:null},{url:"/img/jc_bannerbg.a52d6739.jpg",revision:null},{url:"/img/ldap.fc2cc4a5.png",revision:null},{url:"/img/user.c4a72329.png",revision:null},{url:"/img/wzlogo.3e109d33.png",revision:null},{url:"/index.html",revision:"5218e13a44af5a2bd32c1fa27cae669d"},{url:"/js/145.eb98b5e5.js",revision:null},{url:"/js/153.ce927c18.js",revision:null},{url:"/js/298.eee3266a.js",revision:null},{url:"/js/348.c60d2d12.js",revision:null},{url:"/js/364.c1dc0f2d.js",revision:null},{url:"/js/46.0ae369f2.js",revision:null},{url:"/js/512.f52483a4.js",revision:null},{url:"/js/520.c26389da.js",revision:null},{url:"/js/647.ac635928.js",revision:null},{url:"/js/668.a24f05ca.js",revision:null},{url:"/js/720.edc6a271.js",revision:null},{url:"/js/781.9a7a62f4.js",revision:null},{url:"/js/905.a89c2ce4.js",revision:null},{url:"/js/app.ac04075c.js",revision:null},{url:"/js/chunk-vendors.85f1a82b.js",revision:null},{url:"/logo.png",revision:"4a44830d4e6f75cca23f521e41f5239e"},{url:"/manifest.json",revision:"b931e7ab842b4a3d6aecdd5ed858abad"},{url:"/robots.txt",revision:"735ab4f94fbcd57074377afca324c813"}],{})}));
