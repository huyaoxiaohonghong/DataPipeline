import type { Rule } from 'ant-design-vue/es/form'

/**
 * 通用表单校验规则
 */
export const formRules = {
    /**
     * 必填
     */
    required: (message = '此项为必填项'): Rule => ({
        required: true,
        message,
        trigger: 'blur'
    }),

    /**
     * 必选
     */
    requiredSelect: (message = '请选择'): Rule => ({
        required: true,
        message,
        trigger: 'change'
    }),

    /**
     * 字符串长度范围
     */
    length: (min: number, max: number, message?: string): Rule => ({
        min,
        max,
        message: message || `长度必须在 ${min}-${max} 个字符之间`,
        trigger: 'blur'
    }),

    /**
     * 最小长度
     */
    minLength: (min: number, message?: string): Rule => ({
        min,
        message: message || `长度不能少于 ${min} 个字符`,
        trigger: 'blur'
    }),

    /**
     * 最大长度
     */
    maxLength: (max: number, message?: string): Rule => ({
        max,
        message: message || `长度不能超过 ${max} 个字符`,
        trigger: 'blur'
    }),

    /**
     * 邮箱格式
     */
    email: (message = '请输入有效的邮箱地址'): Rule => ({
        type: 'email',
        message,
        trigger: 'blur'
    }),

    /**
     * 正则表达式
     */
    pattern: (pattern: RegExp, message: string): Rule => ({
        pattern,
        message,
        trigger: 'blur'
    }),

    /**
     * 用户名规则（字母数字下划线，3-20位）
     */
    username: (): Rule[] => [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度必须在 3-20 个字符之间', trigger: 'blur' },
        { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
    ],

    /**
     * 密码规则（6-20位）
     */
    password: (): Rule[] => [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度必须在 6-20 个字符之间', trigger: 'blur' }
    ],

    /**
     * 确认密码规则
     */
    confirmPassword: (getPassword: () => string): Rule[] => [
        { required: true, message: '请确认密码', trigger: 'blur' },
        {
            validator: async (_rule, value) => {
                if (value && value !== getPassword()) {
                    return Promise.reject('两次输入的密码不一致')
                }
                return Promise.resolve()
            },
            trigger: 'blur'
        }
    ],

    /**
     * 手机号规则
     */
    phone: (required = true): Rule[] => {
        const rules: Rule[] = [
            { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号', trigger: 'blur' }
        ]
        if (required) {
            rules.unshift({ required: true, message: '请输入手机号', trigger: 'blur' })
        }
        return rules
    },

    /**
     * URL 规则
     */
    url: (message = '请输入有效的 URL'): Rule => ({
        type: 'url',
        message,
        trigger: 'blur'
    }),

    /**
     * 数字范围
     */
    numberRange: (min: number, max: number, message?: string): Rule => ({
        type: 'number',
        min,
        max,
        message: message || `数值必须在 ${min}-${max} 之间`,
        trigger: 'blur'
    }),

    /**
     * 正整数
     */
    positiveInteger: (message = '请输入正整数'): Rule => ({
        pattern: /^[1-9]\d*$/,
        message,
        trigger: 'blur'
    })
}

/**
 * 创建异步校验器
 */
export function createAsyncValidator(
    asyncFn: (value: any) => Promise<boolean>,
    errorMessage: string
): Rule {
    return {
        validator: async (_rule, value) => {
            if (!value) return Promise.resolve()
            const isValid = await asyncFn(value)
            return isValid ? Promise.resolve() : Promise.reject(errorMessage)
        },
        trigger: 'blur'
    }
}
