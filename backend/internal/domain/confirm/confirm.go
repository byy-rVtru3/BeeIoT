package confirm

import "time"

func (conf *Confirm) generateConfirmationCode() string {
	data := []rune("01234567890123456789")
	conf.rand.Shuffle(len(data), func(i, j int) {
		data[i], data[j] = data[j], data[i]
	})
	return string(data[:6])
}

func (conf *Confirm) NewCode(email string) string {
	code := conf.generateConfirmationCode()
	conf.confirmMutex.Lock()
	conf.confirmCodeMap[email] = code
	conf.confirmMutex.Unlock()
	go func(email, code string) {
		time.Sleep(5 * time.Minute)
		conf.confirmMutex.Lock()
		val, ok := conf.confirmCodeMap[email]
		if !ok || val != code {
			conf.confirmMutex.Unlock()
			return
		}
		delete(conf.confirmCodeMap, email)
		conf.confirmMutex.Unlock()
	}(email, code)
	return code
}

func (conf *Confirm) Verify(email, code string) bool {
	conf.confirmMutex.Lock()
	val, ok := conf.confirmCodeMap[email]
	if !ok || val != code {
		conf.confirmMutex.Unlock()
		return false
	}
	conf.confirmMutex.Unlock()
	return true

}
