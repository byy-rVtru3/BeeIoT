package smtp

import (
	"errors"
	smtpLib "net/smtp"
	"os"

	"github.com/jordan-wright/email"
)

type SMTP struct {
	smtpUser, smtpPass, smtpHost, smtpPort string
	smtpAddress                            string
}

func NewSMTP() (*SMTP, error) {
	data := make([]string, 4)
	for i, elem := range []string{"SMTP_USER", "SMTP_PASS", "SMTP_HOST", "SMTP_PORT"} {
		value, ok := os.LookupEnv(elem)
		if !ok {
			return &SMTP{}, errors.New("environment variable " + elem + " not set")
		}
		data[i] = value
	}
	return &SMTP{
		smtpUser:    data[0],
		smtpPass:    data[1],
		smtpHost:    data[2],
		smtpPort:    data[3],
		smtpAddress: data[2] + ":" + data[3],
	}, nil
}

func (smtp *SMTP) SendConfirmationCode(toEmail, code string) error {
	e := email.NewEmail()
	e.From = "Hive Monitoring <" + smtp.smtpUser + ">"
	e.To = []string{toEmail}
	e.Subject = "Ваш код подтверждения"
	e.Text = []byte("Ваш код подтверждения: " + code +
		"\nЕсли вы не запрашивали код, просто проигнорируйте это письмо.")
	auth := smtpLib.PlainAuth("", smtp.smtpUser, smtp.smtpPass, smtp.smtpHost)
	err := e.Send(smtp.smtpAddress, auth)
	return err
}
