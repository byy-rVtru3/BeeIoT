package main

import (
	"fmt"
	"os"
)

func main() {
	baseURL := "http://localhost:80"
	if len(os.Args) > 1 {
		baseURL = os.Args[1]
	}

	client := NewAPIClient(baseURL)

	fmt.Println("🐝 BeeIoT API Test Client")
	fmt.Println("==================================================")
	fmt.Printf("Base URL: %s\n\n", baseURL)

	if err := client.RunFullAuthFlow(); err != nil {
		fmt.Printf("\n❌ Ошибка: %v\n", err)
		os.Exit(1)
	}

	fmt.Println("\n✅ Все операции выполнены успешно!")
}
