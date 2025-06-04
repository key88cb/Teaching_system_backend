declare module '*.vue' {
    import type { DefineComponent } from 'vue';
    const component: DefineComponent<{}, {}, any>;
    export default component;
}
// 本文件旨在解决Intellij IDEA的TS语言服务问题