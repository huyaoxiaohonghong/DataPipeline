import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 白名单路由（无需登录）
const whiteList = ['/login']

const routes: RouteRecordRaw[] = [
    {
        path: '/',
        name: 'Layout',
        component: () => import('@/layouts/BasicLayout.vue'),
        redirect: '/dashboard',
        meta: { requiresAuth: true },
        children: [
            {
                path: 'dashboard',
                name: 'Dashboard',
                component: () => import('@/views/dashboard/index.vue'),
                meta: { title: '仪表盘', icon: 'DashboardOutlined' }
            },
            {
                path: 'users',
                name: 'UserList',
                component: () => import('@/views/user/UserList.vue'),
                meta: { title: '用户管理', icon: 'UserOutlined' }
            },
            {
                path: 'roles',
                name: 'RoleList',
                component: () => import('@/views/role/RoleList.vue'),
                meta: { title: '角色管理', icon: 'TeamOutlined' }
            },
            {
                path: 'permissions',
                name: 'PermissionList',
                component: () => import('@/views/permission/PermissionList.vue'),
                meta: { title: '权限管理', icon: 'SafetyOutlined' }
            },
            {
                path: 'db-connections',
                name: 'DbConnectionList',
                component: () => import('@/views/db/DbConnectionList.vue'),
                meta: { title: '数据库管理', icon: 'DatabaseOutlined' }
            }
        ]
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/login/index.vue'),
        meta: { title: '登录' }
    },
    {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: () => import('@/views/error/404.vue'),
        meta: { title: '404' }
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes,
    scrollBehavior: () => ({ top: 0 })
})

/**
 * 路由守卫
 */
router.beforeEach(async (to, _from, next) => {
    // 设置页面标题
    document.title = `${to.meta.title || 'DataPipeline'} - DataPipeline`

    const userStore = useUserStore()
    const token = userStore.token

    // 已登录
    if (token) {
        if (to.path === '/login') {
            // 已登录访问登录页，跳转首页
            next({ path: '/' })
        } else {
            // 如果没有用户信息，获取用户信息
            if (!userStore.username) {
                try {
                    await userStore.fetchCurrentUser()
                    next()
                } catch (error) {
                    // Token 无效，跳转登录页
                    userStore.clearUserInfo()
                    next({ path: '/login', query: { redirect: to.fullPath } })
                }
            } else {
                next()
            }
        }
    } else {
        // 未登录
        if (whiteList.includes(to.path)) {
            // 白名单路由直接放行
            next()
        } else {
            // 跳转登录页，并记录目标路径
            next({ path: '/login', query: { redirect: to.fullPath } })
        }
    }
})

export default router
