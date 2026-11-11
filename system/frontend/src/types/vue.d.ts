


declare global {
  interface Window {
    $dialog: import('naive-ui').DialogProviderInst;
    $notice: import('naive-ui').NotificationProviderInst;
    $bar: import('naive-ui').LoadingBarProviderInst;
    $msg: import('naive-ui').MessageProviderInst;
    $modal:import('naive-ui').ModalProviderInst
  };
}

import * as NaiveUI from 'naive-ui';
export as namespace NaiveUI;
export * from 'naive-ui';

declare module 'vue' {
  export interface GlobalComponents {
    MyForm: typeof import('./../components/MyForm/index.vue')['default'] & typeof import('naive-ui')['NForm']
    MyTabel: typeof import('./../components/MyTabel/index.vue')['default'] & typeof import('naive-ui')['NTable']
    MyButton: typeof import('./../components/MyButton/index.vue')['default'] & typeof import('naive-ui')['NButton']
    MyPopconfirm: typeof import('./../components/MyPopconfirm/index.vue')['default'] & typeof import('naive-ui')['NPopconfirm']
    MyPopover: typeof import('./../components/MyPopover/index.vue')['default'] & typeof import('naive-ui')['NPopover']
  }
}
declare module '*.vue' {
  import { defineComponent } from 'vue';
  const component: ReturnType<typeof defineComponent>;
  export default component;
}
