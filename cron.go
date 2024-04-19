package main

import (
	"fmt"
	"net/http"
	"time"
)

func main() {
	url := "https://jaguar-trading-py.onrender.com/hello"
	retryInterval := 5 * time.Second

	stop := make(chan struct{})

	go func() {
		for {
			select {
			case <-stop:
				return
			default:
				fmt.Println("Attempting API call...")
				resp, err := http.Get(url)
				if err != nil {
					fmt.Printf("Error: %s\n", err.Error())
				} else {
					defer resp.Body.Close()
					if resp.StatusCode == http.StatusOK {
						fmt.Println("API call successful!")
						fmt.Println("Attempting second API call...")
						secondURL := "https://jaguar-trading-py.onrender.com/publishMessage"
						secondResp, _ := http.Get(secondURL)
						defer secondResp.Body.Close()
						return
					}
					fmt.Printf("Received status code: %d. Retrying in %s\n", resp.StatusCode, retryInterval)
				}
				time.Sleep(retryInterval)
			}
		}
	}()

	fmt.Println("Press Enter to stop...")
	fmt.Scanln()

	close(stop)

	fmt.Println("Exiting...")
}
