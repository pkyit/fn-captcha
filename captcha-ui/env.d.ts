/// <reference types="vite/client" />

/** .vue 文件模块声明，让 TypeScript 能正确识别 Vue SFC */
declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}
