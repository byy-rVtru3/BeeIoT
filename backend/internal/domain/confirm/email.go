package confirm

import (
	"BeeIOT/internal/domain/interfaces"
	"math/rand"
	"sync"
	"time"
)

type userEmail = string
type userCode = string
type Confirm struct {
	rand           *rand.Rand
	confirmMutex   sync.Mutex
	confirmCodeMap map[userEmail]userCode
	Sender         interfaces.ConfirmSender
}

func NewConfirm(sender interfaces.ConfirmSender) (*Confirm, error) {
	return &Confirm{rand: rand.New(rand.NewSource(time.Now().UnixNano())),
		confirmCodeMap: make(map[userEmail]userCode),
		Sender:         sender}, nil
}
