# #
# # csv_file_path = "./ETF.csv"
# #
# # # Open the CSV file for reading
# # with open(csv_file_path, newline='', encoding='utf-8') as csvfile:
# #     # Create a CSV reader object
# #     csv_reader = csv.reader(csvfile)
# #
# #     # Iterate over each row in the CSV file
# #     for row in csv_reader:
# #         try:
# #             data = yf.download(f'{row[0].strip()}.NS')
# #             data.to_csv(f'./csvs/{row[0].strip()}.csv')
# #         except Exception as e:
# #             print(f'Error downloading {row[0].strip()}')

import csv
import os
import sys

# Install required modules if they are not installed
try:
    import yfinance as yf
except (ModuleNotFoundError, ImportError):
    print("yfinance module not found. Installing...")
    os.system(f"{sys.executable} -m pip install -U yfinance")
finally:
    import yfinance as yf

try:
    import pandas as pd
except (ModuleNotFoundError, ImportError):
    print("pandas module not found. Installing...")
    os.system(f"{sys.executable} -m pip install -U pandas")
finally:
    import pandas as pd

# Ensure 'csvs' directory exists
if not os.path.exists('./csvs'):
    os.makedirs('./csvs')

# Read equity symbols from CSV file
equity_details = pd.read_csv('EQUITY_L.csv')

# Ensure 'SYMBOL' column exists in the CSV
if 'SYMBOL' not in equity_details.columns:
    print("Error: 'SYMBOL' column not found in the CSV file.")
    sys.exit(1)

# Loop through each stock symbol and download data
for name in equity_details.SYMBOL:
    name = name.strip()  # Clean any extra whitespace from symbol
    print(f"Downloading data for {name}")

    try:
        data = yf.download(f'{name}.NS', period="300d")

        # Check if data is empty (in case no data is found)
        if data.empty:
            print(f"No data found for {name}.")
            continue

        # Save data to CSV file
        data.to_csv(f'./csvs/{name}.csv')
        print(f"Data for {name} saved successfully.")
    except Exception as e:
        print(f"Error downloading data for {name}: {str(e)}")