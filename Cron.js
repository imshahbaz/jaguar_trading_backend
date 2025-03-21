const https = require('https');

// API endpoints
//const firstAPIEndpoint = 'https://jaguar-trading-py.onrender.com/hello';
//const secondAPIEndpoint = 'https://jaguar-trading-py.onrender.com/publishMessage';

const firstAPIEndpoint = "https://jaguar-trading-go.onrender.com"
const secondAPIEndpoint = "https://jaguar-trading-go.onrender.com/telegram/publish"

// Call the first API until it returns 200 status code
let firstAPIReached = false;

const callFirstAPI = () => {
    https.get(firstAPIEndpoint, (response) => {
        const { statusCode } = response;

        if (statusCode === 200) {
            console.log('First API call successful (status code 200)');
            firstAPIReached = true;
        } else {
            console.log(`First API call unsuccessful, status code: ${statusCode}, retrying...`);
            // Add some delay before retrying, e.g., setTimeout(callFirstAPI, 1000);
        }
    }).on('error', (err) => {
        console.error('Error occurred while calling the first API:', err.message);
        // Add exception handling
    });
};

// Call the first API initially
callFirstAPI();

// Call the second API after the first API call succeeds
const callSecondAPI = () => {
    https.get(secondAPIEndpoint, (response) => {
        const { statusCode } = response;

        if (statusCode === 200) {
            console.log('Second API call successful (status code 200)');
        } else {
            console.log(`Second API call unsuccessful, status code: ${statusCode}`);
            // Handle other status codes if needed
        }
    }).on('error', (err) => {
        console.error('Error occurred while calling the second API:', err.message);
        // Add exception handling
    });
};

// Check if the first API call is successful before calling the second API
const checkFirstAPIStatus = setInterval(() => {
    if (firstAPIReached) {
        clearInterval(checkFirstAPIStatus);
        callSecondAPI();
    }
}, 1000); // Check every second
