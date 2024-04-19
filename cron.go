package main

import (
	"fmt"
	"net/http"
	"time"
)

func main() {
	url := "https://jaguar-trading-py.onrender.com/hello" // Replace this with the URL of the API you want to call
	retryInterval := 5 * time.Second         // Interval between retries

	// Create a channel to receive signals for graceful shutdown
	stop := make(chan struct{})

	// Start a goroutine to handle the API calls
	go func() {
		for {
			select {
			case <-stop:
				return // Exit the goroutine if stop signal received
			default:
				fmt.Println("Attempting API call...")
				resp, err := http.Get(url)
				if err != nil {
					fmt.Printf("Error: %s\n", err.Error())
				} else {
					defer resp.Body.Close()
					if resp.StatusCode == http.StatusOK {
						fmt.Println("API call successful!")
						return // Exit the goroutine if 200 status code received
					}
					fmt.Printf("Received status code: %d. Retrying in %s\n", resp.StatusCode, retryInterval)
				}
				time.Sleep(retryInterval)
			}
		}
	}()

	// Wait for user input to stop the infinite loop
	fmt.Println("Press Enter to stop...")
	fmt.Scanln()

	// Send stop signal to the goroutine
	close(stop)

	fmt.Println("Exiting...")
}
