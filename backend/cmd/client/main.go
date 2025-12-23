package client

import (
	"fmt"
	"log"
	"os"
	"time"
)

// RunDemo запускает демонстрационные тесты клиента
func RunDemo() {
	fmt.Println("🚀 Starting BeeIoT Integration Tests...")

	// Проверяем запущен ли уже сервер
	testClient := NewTestClient("http://localhost:8080")
	resp, _, err := testClient.MakeRequest("GET", "/health", nil, nil)

	var serverManager *ServerManager

	if err != nil || resp.StatusCode != 200 {
		fmt.Println("📦 Starting test server...")

		// Запускаем тестовый сервер
		serverManager, err = StartTestServer()
		if err != nil {
			log.Fatalf("Failed to start test server: %v", err)
		}
		defer serverManager.Stop()

		fmt.Println("✅ Test server started successfully")
	} else {
		fmt.Println("✅ Using existing server instance")
	}

	// Настраиваем тестовую базу данных
	if err := SetupTestDatabase(); err != nil {
		log.Fatalf("Failed to setup test database: %v", err)
	}
	defer CleanupTestDatabase()

	// Запускаем демонстрационный тест
	fmt.Println("\n🧪 Running demonstration tests...")

	if err := runDemoTests(); err != nil {
		log.Fatalf("Demo tests failed: %v", err)
	}

	fmt.Println("\n🎉 All integration tests completed successfully!")
	fmt.Println("\n💡 To run full test suite, use: go test -v")
}

// runDemoTests запускает демонстрационные тесты
func runDemoTests() error {
	client := NewTestClient("http://localhost:8080")

	// Уникальные данные для демо теста
	timestamp := time.Now().UnixNano()
	testEmail := fmt.Sprintf("demo_user_%d@beeiot.com", timestamp)
	testPassword := "DemoPassword123!"

	fmt.Println("\n1️⃣  Testing user registration...")
	if err := client.Register(testEmail, testPassword); err != nil {
		return fmt.Errorf("registration failed: %w", err)
	}
	fmt.Println("   ✅ User registered successfully")

	fmt.Println("\n2️⃣  Testing user login...")
	if err := client.Login(testEmail, testPassword); err != nil {
		return fmt.Errorf("login failed: %w", err)
	}
	fmt.Println("   ✅ User logged in successfully")
	fmt.Printf("   🔑 Auth token: %s...\n", client.GetAuthToken()[:20])

	fmt.Println("\n3️⃣  Testing queen calendar generation...")
	calendar, err := client.GetQueenCalendar("2025-05-15")
	if err != nil {
		return fmt.Errorf("calendar generation failed: %w", err)
	}
	fmt.Printf("   ✅ Calendar generated for %s\n", calendar.StartDate)
	fmt.Printf("   📅 Egg phase starts: %s\n", calendar.EggPhase.Standing)
	fmt.Printf("   👑 Queen emergence: %s\n", calendar.QueenPhase.EmergenceStart)

	fmt.Println("\n4️⃣  Testing hive creation...")
	hive, err := client.CreateHive("Demo Hive", "Demo Location", "Demonstration hive")
	if err != nil {
		return fmt.Errorf("hive creation failed: %w", err)
	}
	fmt.Printf("   ✅ Hive created with ID: %d\n", hive.ID)
	fmt.Printf("   🏠 Name: %s\n", hive.Name)

	fmt.Println("\n5️⃣  Testing hives list...")
	hives, err := client.GetHives()
	if err != nil {
		return fmt.Errorf("get hives failed: %w", err)
	}
	fmt.Printf("   ✅ Found %d hive(s)\n", len(hives))
	for _, h := range hives {
		fmt.Printf("   🏠 Hive: %s (ID: %d)\n", h.Name, h.ID)
	}

	fmt.Println("\n6️⃣  Testing password change...")
	newPassword := "NewDemoPassword456!"
	if err := client.ChangePassword(testPassword, newPassword); err != nil {
		return fmt.Errorf("password change failed: %w", err)
	}
	fmt.Println("   ✅ Password changed successfully")

	fmt.Println("\n7️⃣  Testing login with new password...")
	newClient := NewTestClient("http://localhost:8080")
	if err := newClient.Login(testEmail, newPassword); err != nil {
		return fmt.Errorf("login with new password failed: %w", err)
	}
	fmt.Println("   ✅ Login with new password successful")

	fmt.Println("\n8️⃣  Cleaning up - deleting demo user...")
	if err := newClient.DeleteUser(); err != nil {
		return fmt.Errorf("user deletion failed: %w", err)
	}
	fmt.Println("   ✅ Demo user deleted successfully")

	return nil
}

// Если файл запущен напрямую
func init() {
	if len(os.Args) > 0 && os.Args[0] != "go" {
		RunDemo()
	}
}
