# Preprocess Data from the CSV file SBER_2.cs
# The time series is difficult to predict by day. Make a row by week or month
# Delete the trend from the series, if there is one.
import numpy as np
# read data from csv file via pandas
import pandas as pd
import matplotlib.pyplot as plt
from statsmodels.graphics.tsaplots import plot_acf


# make dick fuller test method for determining stationarity
def adf(data_column):
    # write if time series is stationary or not
    from statsmodels.tsa.stattools import adfuller
    result = adfuller(data_column)
    print('ADF Statistic: %f' % result[0])
    print('p-value: %f' % result[1])
    print('Critical Values:')
    for key, value in result[4].items():
        print('\t%s: %.3f' % (key, value))

    # When the p-value is less than 0.05, the time series is stationary
    if result[1] < 0.05:
        print("The time series is stationary")
    else:
        print("The time series is not stationary")


# read data from csv file
df = pd.read_csv('../data/SBER_2.csv', sep=',', index_col='DATE', parse_dates=['DATE'])

# print(df.head())

# make a row by week
df = df.resample('M').first()

# log transform the data
df['CLOSE_LOG'] = np.log(df['CLOSE'])

# fill in missing values
df.fillna(method='ffill', inplace=True)


# make method for sesonal decomposition
def seasonal_decomposition(data_column):
    from statsmodels.tsa.seasonal import seasonal_decompose
    decomposition = seasonal_decompose(data_column, model='multiplicative')
    decomposition.plot()
    plt.show()


seasonal_decomposition(df['CLOSE_LOG'])

# df['CLOSE_LOG'].plot()
# plt.show()

# print(df['CLOSE_LOG'].head())

# delete the trend from the series, if there is one
df['CLOSE_LOG_SHIFT1'] = df['CLOSE_LOG'].diff()

# df['CLOSE_LOG_SHIFT1'].plot()
# plt.show()

# print(df['CLOSE_LOG_SHIFT1'].head())

# inverse diff CLOSE_LOG_SHIFT1 to CLOSE_LOG using cumsum
df['CLOSE_LOG_INVERSE'] = df['CLOSE_LOG_SHIFT1'].cumsum() + df['CLOSE_LOG'][0]

# print(df['CLOSE_LOG_INVERSE'].head())

# fill in the missing values
df.fillna(method='ffill', inplace=True)

# test for stationarity
adf(df['CLOSE_LOG_SHIFT1'][1:])


def autocorrelation(data_column):
    plot_acf(data_column, lags=60)
    plt.show()


autocorrelation(df['CLOSE_LOG_SHIFT1'][1:])


# train model
# Train Models on 80% of Time Series
# Predict the remaining 20% (at least 3 points) of the series using models
# Compare the results of the models using MAPE metric

# let's make a method for predict SARIMAX
# use SARIMA model for predict time series data
# plot the data and test data
def train_test_split(data_column, train_size=0.8):
    train_size = int(len(data_column) * train_size)
    return data_column[0:train_size], data_column[train_size:len(data_column)]


def predict_arima(train, test):
    from pmdarima.arima import auto_arima

    # make model
    model = auto_arima(train, start_p=1, start_q=1, max_p=3, max_q=3, m=12, start_P=0, seasonal=True, d=1, D=1,
                       trace=True, error_action='ignore', suppress_warnings=True, stepwise=True)
    model.fit(train)

    # make prediction
    return model.predict(n_periods=len(test))


# make method for predict Holt-Winters method (Triple Exponential Smoothing) for time series data
def predict_holt_winters(train, test):
    from statsmodels.tsa.holtwinters import ExponentialSmoothing

    # make model
    model = ExponentialSmoothing(train, seasonal_periods=12, trend='add', seasonal='add').fit()

    # make prediction
    return model.predict(start=test.index[0], end=test.index[-1])


# make method for predict SARIMAX model for time series data
def predict_sarimax(train, test):
    from statsmodels.tsa.statespace.sarimax import SARIMAX

    # make model
    model = SARIMAX(train, order=(1, 1, 1), seasonal_order=(1, 1, 1, 12))
    model_fit = model.fit(disp=False)

    # make prediction
    return model_fit.predict(start=test.index[0], end=test.index[-1])


def reverse(data_column, diff_value):
    return np.exp(data_column + (data_column.cumsum() + diff_value))


def predict(data_column, data_before_diff):
    train, test = train_test_split(data_column)
    train1, test1 = train_test_split(data_before_diff)

    predictions = \
        [(predict_arima(train, test), 'ARIMA'),
         (predict_holt_winters(train, test), 'Holt-Winters'),
         (predict_sarimax(train, test), 'SARIMAX')]

    plot(train, test, predictions, train1[0], test1[0])


def plot(train, test, predictions, train_diff, test_diff):
    train = reverse(train, train_diff)
    test = reverse(test, test_diff)

    # plot the data and test data
    plt.plot(figsize=(20, 18))
    plt.plot(train, label='Train')
    plt.plot(test, label='Test')
    for (prediction, name) in predictions:
        prediction = reverse(prediction, test_diff)
        plt.plot(prediction, label=name + ' MAPE: {:.2f}'.format(get_mape(prediction, test)))
    plt.legend(loc='upper left', fontsize=8)
    plt.show()


def get_mape(prediction, test):
    return np.mean(np.abs(prediction - test) / np.abs(test))


predict(df['CLOSE_LOG_SHIFT1'][1:], data_before_diff=df['CLOSE_LOG'])
