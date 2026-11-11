import { createRouter, createWebHistory, RouteRecordRaw, createWebHashHistory } from 'vue-router';
import layout from '@/layouts/index.vue';
import userLayout from "@/layouts/userLayout.vue"
declare module 'vue-router' {
    interface _RouteRecordBase {
        hidden?: boolean;
        alwaysShow?: boolean;
    }
}
export const pageRoutes: Array<RouteRecordRaw> = [

]
export const asyncPageRoutes: Array<RouteRecordRaw> = [


]
export const adminRoutes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'layout',
    component: layout,
    redirect: '/data',
    children: [
      {
        path: "/data",
        name: "data",
        meta: { title: "menu.data", icon: "ic--baseline-folder" },
        redirect: '/data/image',
        children: [
          {
            path: '/data/image',
            name: 'image',
            meta: { title: 'menu.image', icon: 'ic--baseline-image !text-inherit' },
            component: () => import('@/views/imagePage/index.vue'),
          },
          {
            path: '/data/video',
            name: 'video',
            meta: { title: 'menu.video', icon: 'ic--baseline-video-camera-back' },
            component: () => import('@/views/videoPage/index.vue'),
          },
          {
            path: '/data/question',
            name: 'question',
            meta: { title: 'menu.question', icon: 'ic--baseline-topic' },
            component: () => import('@/views/question/index.vue'),
          },
          {
            path: '/data/test',
            name: 'test',
            meta: { title: 'menu.test', icon: 'ic--baseline-topic' },
            component: () => import('@/views/test/index.vue'),
          },
          {
            path: '/data/testCate',
            name: 'testCate',
            meta: { title: 'menu.testCate', icon: 'ic--baseline-topic' },
            component: () => import('@/views/test/cate.vue'),
          },
          {
            path: '/data/casebook',
            name: 'casebook',
            meta: { title: 'menu.casebook', icon: 'ic--baseline-snippet-folder' },
            component: () => import('@/views/casebook/index.vue'),
          },
          {
            path: '/data/teacher',
            name: 'teacher',
            hidden:true,
            meta: { title: 'menu.question', icon: 'ic--baseline-topic' },
            component: () => import('@/views/teacher/index.vue'),
          },
          {
            path: '/data/bank',
            name: 'bank',
            hidden:true,
            meta: { title: 'menu.question', icon: 'ic--baseline-topic' },
            component: () => import('@/views/bank/index.vue'),
          },
        ]
      },
      {
        path: '/user',
        name: 'user',
        redirect: '/c',
        meta: { title: 'menu.userAdmin', icon: 'ic--baseline-folder-shared' },
        children: [
          {
            path: '/user/b',
            name: 'b',
            meta: { title: 'menu.userAdmin', icon: 'ic--outline-psychology' },
            component: () => import('@/views/admin/index.vue'),
          },

        ]
      },
      {
        path: "/analysis",
        name: "analysis",
        redirect: "/questionData",
        meta: { title: "menu.analysis", icon: "ic--sharp-troubleshoot" },
        children: [
          {
            path: '/analysis/questionData',
            name: 'questionData',
            meta: { title: 'menu.exercises',icon: 'ic--sharp-content-paste-go' },
            component: () => import('@/views/questionData/index.vue'),
          },
          {
            path: '/analysis/system',
            name: 'system',
            meta: { title: 'menu.log', icon: 'ic--sharp-screen-search-desktop' },
            component: () => import('@/views/system/index.vue'),
          },
        ]
      }
    ],
  }
]
export const userRoutes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'layout',
    component: userLayout,
    redirect: '/home',
    children: [
      {
        path: '/home',
        name: 'home',
        meta: { title: "数据分析", icon: "solar--home-2-bold" },
        component: () => import('@/views/home/index.vue'),
      },
      {
        path: '/course',
        name: 'course',
        meta: { group: 'class' },
        component: () => import('@/views/course/index.vue'),
      },
      {
        path: '/slideGen',
        name: 'slideGen',
        meta: { group: 'class' },
        component: () => import('@/views/slideGen/index.vue'),
      },
      {
        path: '/pastlist',
        name: 'pastlist',
        meta: { group: 'note' },
        component: () => import('@/views/pastList/index.vue'),
      },
      {
        path: '/case',
        name: 'case',
        meta: { group: 'case' },
        component: () => import('@/views/case/index.vue'),
      },
      {
        path: '/viewppt',
        name: 'viewppt',
        meta: { group: 'note' },
        component: () => import('@/views/viewppt/index.vue'),
      },
      {
        path: "/study",
        name: "study",
        meta: { group: 'class' },
        component: () => import('@/views/study/index.vue'),
      },
      {
        path: "/exam",
        name: "exam",
        meta: { group: 'exercise' },
        component: () => import('@/views/exam/index.vue'),
      },
      {
        path: '/testCate',
        name: 'testCate',
        meta: { group: 'test' },
        component: () => import('@/views/testPage/cate.vue'),
      },
      {
        path: "/test",
        name: "test",
        meta: { group: 'test' },
        component: () => import('@/views/testPage/index.vue'),
      },
      {
        path: "/note",
        name: "note",
        meta: { group: 'note' },
        component: () => import('@/views/note/index.vue'),
      },
      {
        path: "/report",
        name: "report",
        meta: { group: 'note' },
        component: () => import('@/views/report/index.vue'),
      },
      {
        path: "/forum",
        name: "forum",
        meta: { group: 'discuss' },
        component: () => import('@/views/forum/index.vue'),
      },
      {
        path: "/news",
        name: "news",
        component: () => import('@/views/news/index.vue'),
      },
      {
        path: "/postWritings",
        name: "postWritings",
        meta: { group: 'discuss' },
        component: () => import('@/views/postWritings/index.vue'),
      },

    ]
  },

]
export const noLoyoutRoutes: Array<RouteRecordRaw> = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/login/index.vue'),
    hidden: true
  },
]
export const staticRoutes: Array<RouteRecordRaw> = [
  {
    path: '/:pathMatch(.*)*',  
    component: ()=>import("@/views/404/index.vue"),
    hidden: true
  },
]

const _createRouter = () =>
    createRouter({
        history: createWebHistory(),
      scrollBehavior: () => ({ top: 0 }),
        routes: [...noLoyoutRoutes,...staticRoutes]
    });

let router = _createRouter();
export function resetRouter() {
    router = _createRouter();
}
export default router;
