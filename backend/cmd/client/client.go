package main

import (
	"bufio"
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"strings"
	"time"
)

type APIClient struct {
	BaseURL string
	Client  *http.Client
	Token   string
	Email   string
	Name    string
}

type Response struct {
	Status  string                 `json:"status"`
	Message string                 `json:"message"`
	Data    map[string]interface{} `json:"data,omitempty"`
}

type RegistrationRequest struct {
	Email    string `json:"email"`
	Name     string `json:"name"`
	Password string `json:"password"`
}

type ConfirmRequest struct {
	Email string `json:"email"`
	Code  string `json:"code"`
}

type LoginRequest struct {
	Email    string `json:"email"`
	Password string `json:"password"`
}

type ChangePasswordRequest struct {
	Email    string `json:"email"`
	Password string `json:"password"`
}

func NewAPIClient(baseURL string) *APIClient {
	return &APIClient{
		BaseURL: baseURL,
		Client: &http.Client{
			Timeout: 10 * time.Second,
		},
	}
}

// RunFullAuthFlow выполняет полный цикл аутентификации
func (c *APIClient) RunFullAuthFlow() error {
	// Генерация уникального email с timestamp
	timestamp := time.Now().Unix()
	c.Email = "i.statsenko@g.nsu.ru"
	c.Name = fmt.Sprintf("TestUser_%d", timestamp)
	oldPassword := "SecurePassword123!"
	newPassword := "NewSecurePassword456!"

	// Шаг 1: Регистрация
	fmt.Println("📝 Шаг 1: Регистрация пользователя")
	fmt.Printf("   Email: %s\n", c.Email)
	fmt.Printf("   Name: %s\n", c.Name)
	if err := c.Register(c.Email, c.Name, oldPassword); err != nil {
		return fmt.Errorf("регистрация: %w", err)
	}

	// Шаг 2: Подтверждение регистрации
	fmt.Println("\n✉️  Шаг 2: Подтверждение регистрации")
	code := c.readCodeFromConsole("Введите код подтверждения из email для регистрации")
	if err := c.ConfirmRegistration(c.Email, code); err != nil {
		return fmt.Errorf("подтверждение регистрации: %w", err)
	}

	// Шаг 3: Логин
	fmt.Println("\n🔐 Шаг 3: Вход в систему")
	if err := c.Login(c.Email, oldPassword); err != nil {
		return fmt.Errorf("логин: %w", err)
	}
	fmt.Printf("   ✓ Получен JWT токен: %s...\n", c.Token[:50])

	// Шаг 4: Смена пароля
	fmt.Println("\n🔑 Шаг 4: Смена пароля")
	if err := c.ChangePassword(c.Email, newPassword); err != nil {
		return fmt.Errorf("смена пароля: %w", err)
	}

	// Шаг 5: Подтверждение смены пароля
	fmt.Println("\n✉️  Шаг 5: Подтверждение смены пароля")
	code = c.readCodeFromConsole("Введите код подтверждения из email для смены пароля")
	if err := c.ConfirmPasswordChange(c.Email, code); err != nil {
		return fmt.Errorf("подтверждение смены пароля: %w", err)
	}

	// Шаг 6: Повторный логин с новым паролем
	fmt.Println("\n🔐 Шаг 6: Вход с новым паролем")
	if err := c.Login(c.Email, newPassword); err != nil {
		return fmt.Errorf("логин с новым паролем: %w", err)
	}
	fmt.Printf("   ✓ Получен новый JWT токен: %s...\n", c.Token[:50])

	// Шаг 7: Выход из системы
	fmt.Println("\n🚪 Шаг 7: Выход из системы")
	if err := c.Logout(); err != nil {
		return fmt.Errorf("выход: %w", err)
	}

	return nil
}

// Register регистрирует нового пользователя
func (c *APIClient) Register(email, name, password string) error {
	reqBody := RegistrationRequest{
		Email:    email,
		Name:     name,
		Password: password,
	}

	resp, err := c.doRequest("POST", "/api/auth/registration", reqBody, false)
	if err != nil {
		return err
	}

	fmt.Printf("   ✓ %s\n", resp.Message)
	return nil
}

// ConfirmRegistration подтверждает регистрацию кодом
func (c *APIClient) ConfirmRegistration(email, code string) error {
	reqBody := ConfirmRequest{
		Email: email,
		Code:  code,
	}

	resp, err := c.doRequest("POST", "/api/auth/confirm/registration", reqBody, false)
	if err != nil {
		return err
	}

	fmt.Printf("   ✓ %s\n", resp.Message)
	return nil
}

// Login выполняет вход в систему
func (c *APIClient) Login(email, password string) error {
	reqBody := LoginRequest{
		Email:    email,
		Password: password,
	}

	resp, err := c.doRequest("POST", "/api/auth/login", reqBody, false)
	if err != nil {
		return err
	}

	// Извлекаем токен из ответа
	if tokenData, ok := resp.Data["token"].(string); ok {
		c.Token = tokenData
	} else {
		return fmt.Errorf("токен не найден в ответе")
	}

	fmt.Printf("   ✓ %s\n", resp.Message)
	return nil
}

// ChangePassword инициирует смену пароля
func (c *APIClient) ChangePassword(email, newPassword string) error {
	reqBody := ChangePasswordRequest{
		Email:    email,
		Password: newPassword,
	}

	resp, err := c.doRequest("POST", "/api/auth/change", reqBody, false)
	if err != nil {
		return err
	}

	fmt.Printf("   ✓ %s\n", resp.Message)
	return nil
}

// ConfirmPasswordChange подтверждает смену пароля кодом
func (c *APIClient) ConfirmPasswordChange(email, code string) error {
	reqBody := ConfirmRequest{
		Email: email,
		Code:  code,
	}

	resp, err := c.doRequest("POST", "/api/auth/confirm/password", reqBody, false)
	if err != nil {
		return err
	}

	fmt.Printf("   ✓ %s\n", resp.Message)
	return nil
}

// Logout выходит из системы (требует токен)
func (c *APIClient) Logout() error {
	resp, err := c.doRequest("DELETE", "/api/auth/logout", nil, true)
	if err != nil {
		return err
	}

	fmt.Printf("   ✓ %s\n", resp.Message)
	c.Token = "" // Очищаем токен
	return nil
}

// doRequest выполняет HTTP запрос
func (c *APIClient) doRequest(method, path string, body interface{}, useAuth bool) (*Response, error) {
	var reqBody io.Reader
	if body != nil {
		jsonData, err := json.Marshal(body)
		if err != nil {
			return nil, fmt.Errorf("marshal json: %w", err)
		}
		reqBody = bytes.NewBuffer(jsonData)
	}

	url := c.BaseURL + path
	req, err := http.NewRequest(method, url, reqBody)
	if err != nil {
		return nil, fmt.Errorf("создание запроса: %w", err)
	}

	req.Header.Set("Content-Type", "application/json")

	// Добавляем токен авторизации если требуется
	if useAuth && c.Token != "" {
		req.Header.Set("Authorization", "Bearer "+c.Token)
	}

	// Выполняем запрос
	httpResp, err := c.Client.Do(req)
	if err != nil {
		return nil, fmt.Errorf("выполнение запроса: %w", err)
	}
	defer httpResp.Body.Close()

	// Читаем ответ
	respBody, err := io.ReadAll(httpResp.Body)
	if err != nil {
		return nil, fmt.Errorf("чтение ответа: %w", err)
	}

	// Проверяем статус код
	if httpResp.StatusCode < 200 || httpResp.StatusCode >= 300 {
		return nil, fmt.Errorf("HTTP %d: %s", httpResp.StatusCode, string(respBody))
	}

	// Парсим JSON ответ
	var resp Response
	if err := json.Unmarshal(respBody, &resp); err != nil {
		return nil, fmt.Errorf("парсинг json: %w (body: %s)", err, string(respBody))
	}

	return &resp, nil
}

// readCodeFromConsole читает код подтверждения из консоли
func (c *APIClient) readCodeFromConsole(prompt string) string {
	reader := bufio.NewReader(os.Stdin)
	fmt.Printf("   %s: ", prompt)

	code, _ := reader.ReadString('\n')
	code = strings.TrimSpace(code)

	return code
}
