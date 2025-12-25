package com.app.controller;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class sendVerificationEmail {

    // Méthode pour envoyer le mail
    public static boolean sendEmail(String recipientEmail, String subject, String body) {
        // Configuration SMTP (ici Gmail)
        final String senderEmail = "ouladsmaneissam@gmail.com"; // ton email
        final String senderPassword = "keph izyz dsmo odzp"; // mot de passe application Gmail

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Session mail avec authentification
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(recipientEmail)
            );
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("Email envoyé à " + recipientEmail);
            return true;

        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Exemple de génération de code aléatoire à 6 chiffres
    public static String generateVerificationCode() {
        int code = (int)(Math.random() * 900000) + 100000;
        return String.valueOf(code);
    }

    // Test rapide
    public static void main(String[] args) {
        String code = generateVerificationCode();
        sendEmail("ouladsmaneissam2@gmail.com", "Code de vérification", "Votre code est : " + code);

    }
}
