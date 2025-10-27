package handlers

import "BeeIOT/internal/domain/interfaces"

type Handler struct {
	db interfaces.DB
}

func NewHandler(db interfaces.DB) *Handler {
	return &Handler{db: db}
}
