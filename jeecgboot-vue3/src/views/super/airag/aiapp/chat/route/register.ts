import type { App } from 'vue';
import { router } from "/@/router";
import type { RouteRecordRaw } from "vue-router";
import { LAYOUT } from "@/router/constant";

const ChatRoutes: RouteRecordRaw[] = [
  {
    path: "/ai/app/chat/:appId",
    name: "ai-chat-@appId-@modeType",
    component: () => import("/@/views/super/airag/aiapp/chat/AiChat.vue"),
    meta: {
      title: 'AI Trò chuyện',
      ignoreAuth: true,
    },
  },  
  {
    path: "/ai/app/chatIcon/:appId",
    name: "ai-chatIcon-@appId",
    component: () => import("/@/views/super/airag/aiapp/chat/AiChatIcon.vue"),
    meta: {
      title: 'AI Trò chuyện',
      ignoreAuth: true,
    },
  },
  {
    path: '/ai/chat',
    name: 'aiChat',
    component: LAYOUT,
    meta: {
      title: 'ai trò chuyện',
    },
    children: [
      {
        path: "/ai/chat/:appId",
        name: "ai-chat-@appId",
        component: () => import("/@/views/super/airag/aiapp/chat/AiChat.vue"),
        meta: {
          title:'Trợ lý AI',
          ignoreAuth: false,
        },
      },
      {
        path: "/ai/chat",
        name: "ai-chat",
        component: () => import("/@/views/super/airag/aiapp/chat/AiChat.vue"),
        meta: {
          title:'Trợ lý AI',
          ignoreAuth: false,
        },
      }
    ],
  },
  {
    path: '/ai/chat/portal',
    name: 'ai-chat-portal',
    component: () => import('/@/views/super/airag/aiapp/chat/portal/AppPortal.vue'),
    meta: {
      title: 'AI Trò chuyện',
      ignoreAuth: false,
    },
  },
]

/** Đăng ký định tuyến */
export async function register(app: App) {
  await registerMyAppRouter(app);
  console.log('[Định tuyến trò chuyện] Đăng ký hoàn thành!');
}

async function registerMyAppRouter(_: App) {
  for(let appRoute of ChatRoutes){
    await router.addRoute(appRoute);
  }
}
