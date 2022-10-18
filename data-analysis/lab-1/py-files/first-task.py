import pandas as pd
import matplotlib.pyplot as plt
import matplotlib.dates as mdates

companies = ['Amazon', 'Google', 'Netflix', 'Facebook', 'Apple']

company_stockprice_data_path = {
    'Amazon': 'dataset/Amazon_Historical_StockPrice2.csv',
    'Apple': 'dataset/Apple_Historical_StockPrice2.csv',
    'Facebook': 'dataset/Facebook_Historical_StockPrice2.csv',
    'Google': 'dataset/Google_Historical_StockPrice2.csv',
    'Netflix': 'dataset/Netflix_Historical_StockPrice2.csv',
}

company_stockprice_data = dict.fromkeys(companies)
datefmt = mdates.DateFormatter('%Y-%m')
datelocator = mdates.MonthLocator(interval=3)

for company in companies:
    company_stockprice_data[company] = pd.read_csv(company_stockprice_data_path[company]).dropna(how='any')


def configure_plot():
    plt.figure(figsize=(12, 10), dpi=100)
    plt.gca().xaxis.set_major_formatter(datefmt)
    plt.gca().xaxis.set_major_locator(datelocator)
    plt.gcf().autofmt_xdate()
