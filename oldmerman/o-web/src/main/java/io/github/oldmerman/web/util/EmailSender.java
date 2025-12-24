package io.github.oldmerman.web.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@RequiredArgsConstructor
public class EmailSender {

    private final JavaMailSender sender;

    private final String from = "oldmerman@qq.com";

    private final String subject = "老鱼人博客的登录检验操作...";

    private final String text =
            """
            <!DOCTYPE html>
            <html lang="zh-CN">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>验证您的注册邮箱</title>
                <style>
                    body {
                        margin: 0;
                        padding: 0;
                        font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
                        background-color: #f4f7f9;
                        color: #333;
                    }
                    table {
                        border-spacing: 0;
                    }
                    img {
                        border: 0;
                    }
                    .wrapper {
                        width: 100%;
                        table-layout: fixed;
                        background-color: #f4f7f9;
                        padding-bottom: 40px;
                        border-radius: 8px;
                    }
                    .main {
                        background-color: #ffffff;
                        margin: 0 auto;
                        width: 100%;
                        max-width: 500px;
                        border-radius: 8px;
                        box-shadow: 0 4px 10px rgba(0,0,0,0.05);
                        overflow: hidden;
                        margin-top: 40px;
                    }
                    /* 内容区域 */
                    .content {
                        padding: 30px;
                        line-height: 1.6;
                    }
                    .code-box {
                        background-color: #f0f7ff;
                        border: 1px dashed #90caf9;
                        border-radius: 4px;
                        text-align: center;
                        margin: 25px 0;
                        padding: 15px;
                    }
                    .code {
                        font-size: 32px;
                        font-weight: bold;
                        color: #1565c0;
                        letter-spacing: 5px;
                    }
                    /* 页脚 */
                    .footer {
                        text-align: center;
                        font-size: 12px;
                        color: #999;
                        padding: 8px;
                    }
                </style>
            </head>
            <body>
                <div class="wrapper">
                    <table class="main">
                        <tr>
                            <td class="content">
                                <p>您好！</p>
                                <p>欢迎访问浏览老鱼人的博客，请在验证页面输入下方的验证码：</p>
                                <div class="code-box">
                                    <span class="code">884822</span> <!-- 此处替换为动态验证码 -->
                                </div>
                                <p>该验证码在 <strong>10分钟</strong> 内有效。如果这不是您本人操作，请忽略此邮件。</p>
                                <p>祝您有个愉快旅程！</p>
                            </td>
                        </tr>
                    </table>
                    <div class="footer">
                        <p>© 2025 老鱼人. 保留所有权利。</p>
                        <p>这是一封系统自动发送的邮件，请勿直接回复。</p>
                    </div>
                </div>
            </body>
            </html>""";

    public String sendCheckEmail(String target) throws MessagingException {
        Random random = new Random();
        StringBuilder code = new StringBuilder("" + random.nextInt(1, 9));
        for (int i = 0; i < 5; i++) {
            code.append(random.nextInt(10));
        }
        MimeMessage mail = sender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mail);
        mimeMessageHelper.setSubject(subject);
        mimeMessageHelper.setText(text.replace("884822",code.toString()), true);
        mimeMessageHelper.setFrom(from);
        mimeMessageHelper.setTo(target);
        sender.send(mail);
        return code.toString();
    }
}
