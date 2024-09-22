import csv
import os
import sys

try:
    import yfinance as yf
except (ModuleNotFoundError, ImportError):
    print("xlwings module not found")
    os.system(f"{sys.executable} -m pip install -U yfinance")
finally:
    import yfinance as yf

try:
    import pandas as pd
except (ModuleNotFoundError, ImportError):
    print("pandas module not found")
    os.system(f"{sys.executable} -m pip install -U pandas")
finally:
    import pandas as pd

equity_details = pd.read_csv('EQUITY_L.csv')

for name  in equity_details.SYMBOL:
    try:
        data = yf.download(f'{name}.NS')
        data.to_csv(f'./csvs/{name}.csv')
    except Exception as e:
        print(f'Error downloading {name}')
#
# csv_file_path = "./ETF.csv"
#
# # Open the CSV file for reading
# with open(csv_file_path, newline='', encoding='utf-8') as csvfile:
#     # Create a CSV reader object
#     csv_reader = csv.reader(csvfile)
#
#     # Iterate over each row in the CSV file
#     for row in csv_reader:
#         try:
#             data = yf.download(f'{row[0].strip()}.NS')
#             data.to_csv(f'./csvs/{row[0].strip()}.csv')
#         except Exception as e:
#             print(f'Error downloading {row[0].strip()}')
