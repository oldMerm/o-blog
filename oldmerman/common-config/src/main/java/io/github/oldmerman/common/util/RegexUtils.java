package io.github.oldmerman.common.util;

import java.util.regex.Pattern;

/**
 * 正则表达式工具类
 * 用于验证各种常见格式：手机号、密码、身份证、邮箱等
 * 
 * @author ai-kimi
 * @date 2025-11-01
 */
public class RegexUtils {
    
    /**
     * 手机号正则表达式
     * 支持：13x、14x、15x、16x、17x、18x、19x号段
     */
    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";
    
    /**
     * 密码正则表达式
     * 要求：8-20位，必须包含字母和数字，可包含特殊字符
     */
    private static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{8,20}$";
    
    /**
     * 强密码正则表达式
     * 要求：8-20位，必须包含大写字母、小写字母、数字、特殊字符中的至少3种
     */
    private static final String STRONG_PASSWORD_REGEX = "^(?![a-zA-Z]+$)(?![A-Z0-9]+$)(?![A-Z\\W_]+$)(?![a-z0-9]+$)(?![a-z\\W_]+$)(?![0-9\\W_]+$)[a-zA-Z0-9\\W_]{8,20}$";
    
    /**
     * 邮箱正则表达式
     * 支持常见邮箱格式
     */
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    
    /**
     * 身份证号码正则表达式
     * 支持18位身份证（包括最后一位的X）
     */
    private static final String ID_CARD_REGEX = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$";
    
    /**
     * 用户名正则表达式
     * 要求：3-20位，支持字母、数字、下划线，必须以字母开头
     */
    private static final String USERNAME_REGEX = "^[A-Za-z][A-Za-z0-9_]{2,19}$";
    
    /**
     * 验证码正则表达式（6位数字）
     */
    private static final String VERIFICATION_CODE_REGEX = "^\\d{6}$";
    
    /**
     * 手机号格式验证
     * @param phone 手机号
     * @return 是否有效
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && Pattern.matches(PHONE_REGEX, phone);
    }
    
    /**
     * 密码格式验证（基础强度）
     * @param password 密码
     * @return 是否有效
     */
    public static boolean isValidPassword(String password) {
        return password != null && Pattern.matches(PASSWORD_REGEX, password);
    }
    
    /**
     * 强密码格式验证
     * @param password 密码
     * @return 是否有效
     */
    public static boolean isValidStrongPassword(String password) {
        return password != null && Pattern.matches(STRONG_PASSWORD_REGEX, password);
    }
    
    /**
     * 邮箱格式验证
     * @param email 邮箱
     * @return 是否有效
     */
    public static boolean isValidEmail(String email) {
        return email != null && Pattern.matches(EMAIL_REGEX, email);
    }
    
    /**
     * 身份证格式验证
     * @param idCard 身份证号码
     * @return 是否有效
     */
    public static boolean isValidIdCard(String idCard) {
        return idCard != null && Pattern.matches(ID_CARD_REGEX, idCard);
    }
    
    /**
     * 用户名格式验证
     * @param username 用户名
     * @return 是否有效
     */
    public static boolean isValidUsername(String username) {
        return username != null && Pattern.matches(USERNAME_REGEX, username);
    }
    
    /**
     * 验证码格式验证（6位数字）
     * @param code 验证码
     * @return 是否有效
     */
    public static boolean isValidVerificationCode(String code) {
        return code != null && Pattern.matches(VERIFICATION_CODE_REGEX, code);
    }
    
    /**
     * 获取密码强度等级
     * @param password 密码
     * @return 强度等级：1-弱，2-中，3-强
     */
    public static int getPasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            return 1;
        }
        
        int strength = 0;
        
        // 包含小写字母
        if (Pattern.matches(".*[a-z].*", password)) {
            strength++;
        }
        
        // 包含大写字母
        if (Pattern.matches(".*[A-Z].*", password)) {
            strength++;
        }
        
        // 包含数字
        if (Pattern.matches(".*\\d.*", password)) {
            strength++;
        }
        
        // 包含特殊字符
        if (Pattern.matches(".*[@$!%*?&].*", password)) {
            strength++;
        }
        
        // 根据强度返回等级
        if (strength <= 2) {
            return 1; // 弱
        } else if (strength == 3) {
            return 2; // 中
        } else {
            return 3; // 强
        }
    }
    
    /**
     * 隐藏手机号中间四位
     * @param phone 手机号
     * @return 脱敏后的手机号
     */
    public static String maskPhone(String phone) {
        if (isValidPhone(phone)) {
            return phone.substring(0, 3) + "****" + phone.substring(7);
        }
        return phone;
    }
    
    /**
     * 隐藏邮箱部分信息
     * @param email 邮箱
     * @return 脱敏后的邮箱
     */
    public static String maskEmail(String email) {
        if (isValidEmail(email)) {
            String[] parts = email.split("@");
            if (parts[0].length() > 3) {
                return parts[0].substring(0, 3) + "***@" + parts[1];
            } else {
                return parts[0] + "***@" + parts[1];
            }
        }
        return email;
    }
    
    /**
     * 隐藏身份证部分信息
     * @param idCard 身份证
     * @return 脱敏后的身份证
     */
    public static String maskIdCard(String idCard) {
        if (isValidIdCard(idCard)) {
            return idCard.substring(0, 6) + "********" + idCard.substring(14);
        }
        return idCard;
    }
}