package client

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"time"
)

// Структуры для API запросов/ответов
type RegistrationRequest struct {
	Email    string `json:"email"`
	Password string `json:"password"`
}

type LoginRequest struct {
	Email    string `json:"email"`
	Password string `json:"password"`
}

type LoginResponse struct {
	Token string `json:"token"`
}

type ChangePasswordRequest struct {
	OldPassword string `json:"old_password"`
	NewPassword string `json:"new_password"`
}

type QueenCalendarRequest struct {
	StartDate string `json:"start_date"`
}

type QueenCalendarResponse struct {
	StartDate string `json:"start_date"`
	EggPhase  struct {
		Standing string `json:"standing"`
		Tilted   string `json:"tilted"`
		Lying    string `json:"lying"`
	} `json:"egg_phase"`
	LarvaPhase struct {
		Start  string `json:"start"`
		Day1   string `json:"day1"`
		Day2   string `json:"day2"`
		Day3   string `json:"day3"`
		Day4   string `json:"day4"`
		Day5   string `json:"day5"`
		Sealed string `json:"sealed"`
	} `json:"larva_phase"`
	PupaPhase struct {
		Start     string `json:"start"`
		End       string `json:"end"`
		Duration  string `json:"duration"`
		Selection string `json:"selection"`
	} `json:"pupa_phase"`
	QueenPhase struct {
		EmergenceStart      string `json:"emergence_start"`
		EmergenceEnd        string `json:"emergence_end"`
		MaturationStart     string `json:"maturation_start"`
		MaturationEnd       string `json:"maturation_end"`
		MatingFlightStart   string `json:"mating_flight_start"`
		MatingFlightEnd     string `json:"mating_flight_end"`
		InseminationStart   string `json:"insemination_start"`
		InseminationEnd     string `json:"insemination_end"`
		EggLayingCheckStart string `json:"egg_laying_check_start"`
		EggLayingCheckEnd   string `json:"egg_laying_check_end"`
	} `json:"queen_phase"`
}

type HiveResponse struct {
	ID          int    `json:"id"`
	Name        string `json:"name"`
	Location    string `json:"location"`
	Description string `json:"description"`
}

type CreateHiveRequest struct {
	Name        string `json:"name"`
	Location    string `json:"location"`
	Description string `json:"description"`
}

// HTTP клиент для тестирования
type TestClient struct {
	baseURL    string
	httpClient *http.Client
	authToken  string
}

func NewTestClient(baseURL string) *TestClient {
	return &TestClient{
		baseURL: baseURL,
		httpClient: &http.Client{
			Timeout: 10 * time.Second,
		},
	}
}

func (c *TestClient) makeRequest(method, endpoint string, body interface{}, headers map[string]string) (*http.Response, []byte, error) {
	var reqBody io.Reader
	if body != nil {
		jsonData, err := json.Marshal(body)
		if err != nil {
			return nil, nil, fmt.Errorf("failed to marshal request body: %w", err)
		}
		reqBody = bytes.NewBuffer(jsonData)
	}

	req, err := http.NewRequest(method, c.baseURL+endpoint, reqBody)
	if err != nil {
		return nil, nil, fmt.Errorf("failed to create request: %w", err)
	}

	// Устанавливаем заголовки
	req.Header.Set("Content-Type", "application/json")
	for key, value := range headers {
		req.Header.Set(key, value)
	}

	// Добавляем токен авторизации если есть
	if c.authToken != "" {
		req.Header.Set("Authorization", "Bearer "+c.authToken)
	}

	resp, err := c.httpClient.Do(req)
	if err != nil {
		return nil, nil, fmt.Errorf("failed to make request: %w", err)
	}
	defer resp.Body.Close()

	respBody, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, nil, fmt.Errorf("failed to read response body: %w", err)
	}

	return resp, respBody, nil
}

func (c *TestClient) Register(email, password string) error {
	req := RegistrationRequest{
		Email:    email,
		Password: password,
	}

	resp, _, err := c.makeRequest("POST", "/api/auth/register", req, nil)
	if err != nil {
		return err
	}

	if resp.StatusCode != http.StatusCreated && resp.StatusCode != http.StatusOK {
		return fmt.Errorf("registration failed with status %d", resp.StatusCode)
	}

	return nil
}

func (c *TestClient) Login(email, password string) error {
	req := LoginRequest{
		Email:    email,
		Password: password,
	}

	resp, body, err := c.makeRequest("POST", "/api/auth/login", req, nil)
	if err != nil {
		return err
	}

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("login failed with status %d: %s", resp.StatusCode, string(body))
	}

	var loginResp LoginResponse
	if err := json.Unmarshal(body, &loginResp); err != nil {
		return fmt.Errorf("failed to parse login response: %w", err)
	}

	c.authToken = loginResp.Token
	return nil
}

func (c *TestClient) ChangePassword(oldPassword, newPassword string) error {
	req := ChangePasswordRequest{
		OldPassword: oldPassword,
		NewPassword: newPassword,
	}

	resp, body, err := c.makeRequest("PUT", "/api/auth/password", req, nil)
	if err != nil {
		return err
	}

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("change password failed with status %d: %s", resp.StatusCode, string(body))
	}

	return nil
}

func (c *TestClient) GetQueenCalendar(startDate string) (*QueenCalendarResponse, error) {
	req := QueenCalendarRequest{
		StartDate: startDate,
	}

	resp, body, err := c.makeRequest("POST", "/api/queen/calendar", req, nil)
	if err != nil {
		return nil, err
	}

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("get queen calendar failed with status %d: %s", resp.StatusCode, string(body))
	}

	var calendar QueenCalendarResponse
	if err := json.Unmarshal(body, &calendar); err != nil {
		return nil, fmt.Errorf("failed to parse calendar response: %w", err)
	}

	return &calendar, nil
}

func (c *TestClient) CreateHive(name, location, description string) (*HiveResponse, error) {
	req := CreateHiveRequest{
		Name:        name,
		Location:    location,
		Description: description,
	}

	resp, body, err := c.makeRequest("POST", "/api/hive", req, nil)
	if err != nil {
		return nil, err
	}

	if resp.StatusCode != http.StatusCreated && resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("create hive failed with status %d: %s", resp.StatusCode, string(body))
	}

	var hive HiveResponse
	if err := json.Unmarshal(body, &hive); err != nil {
		return nil, fmt.Errorf("failed to parse hive response: %w", err)
	}

	return &hive, nil
}

func (c *TestClient) GetHives() ([]HiveResponse, error) {
	resp, body, err := c.makeRequest("GET", "/api/hive", nil, nil)
	if err != nil {
		return nil, err
	}

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("get hives failed with status %d: %s", resp.StatusCode, string(body))
	}

	var hives []HiveResponse
	if err := json.Unmarshal(body, &hives); err != nil {
		return nil, fmt.Errorf("failed to parse hives response: %w", err)
	}

	return hives, nil
}

func (c *TestClient) DeleteUser() error {
	resp, body, err := c.makeRequest("DELETE", "/api/auth/user", nil, nil)
	if err != nil {
		return err
	}

	if resp.StatusCode != http.StatusOK && resp.StatusCode != http.StatusNoContent {
		return fmt.Errorf("delete user failed with status %d: %s", resp.StatusCode, string(body))
	}

	return nil
}

// MakeRequest экспортированный метод для внешнего использования
func (c *TestClient) MakeRequest(method, endpoint string, body interface{}, headers map[string]string) (*http.Response, []byte, error) {
	return c.makeRequest(method, endpoint, body, headers)
}

// GetAuthToken возвращает текущий токен аутентификации
func (c *TestClient) GetAuthToken() string {
	return c.authToken
}
