import os
import time
from datetime import datetime
from math import radians, sin, cos, sqrt, atan2

import numpy as np
import pandas as pd
import seaborn as sns
from matplotlib import pyplot as plt
from numpy import Infinity
from sklearn.linear_model import LinearRegression
from sklearn.metrics import mean_absolute_error, r2_score, mean_absolute_percentage_error
from sklearn.model_selection import train_test_split


# https://www.kaggle.com/datasets/lildatascientist/raifhackds2021fall

# reform_house_population_1000 - Coefficient of the number of people living within a radius of 1 km by source Reform
# reform_mean_floor_count_1000 - Average number of storeys of houses within a radius of 1 km according to the source
# Reform
# reform_mean_year_building_1000 - Average value of the year of construction of houses within a radius of 1 km
# according to the source Reform


def analyze_variance(df_train, df_test):
    print(f'\n{" Analyzing Features variance ":.^100}\n')
    best_error = Infinity
    best_var = 0
    best_features = []
    best_features_to_drop = []
    errors = []
    variances = []
    # best var 2
    # vals = [0.000001, 0.00001, 0.0001, 0.001, 0.1]
    vals = [0.001]
    for var in vals:
        features = df_train.var()[df_train.var() > var].index.tolist()
        fearures_to_drop = df_train.var()[df_train.var() <= var].index.tolist()

        train_x = df_train[features]
        train_y = df_test

        model = LinearRegression()
        model.fit(train_x, train_y)
        y_pred = model.predict(train_x)

        r2 = r2_score(train_y, y_pred)
        print(f'Correlation {var}: {r2}')

        error = mean_absolute_percentage_error(train_y, y_pred)
        print(f'MAPE {var}: {error}')

        if error < best_error:
            best_error = error
            best_features = features
            best_var = var
            best_features_to_drop = fearures_to_drop
        errors.append(error)
        variances.append(var)

    print(f'\nBest features stat: {best_var} {best_error} - \n{best_features}')
    print(f'\nFeatures to drop: {best_features_to_drop}')

    plt.plot(variances, errors)
    plt.xlabel('Variance')
    plt.ylabel('MAPE')
    plt.show()

    print(f'\n{" Analyzing Features variance ":.^100}\n')
    return best_features_to_drop


def correlation(df, threshold):
    col_corr = set()  # Set of all the names of deleted columns
    corr_matrix = df.corr()
    for i in range(len(corr_matrix.columns)):
        for j in range(i):
            if abs(corr_matrix.iloc[i, j]) > threshold:  # we are interested in absolute coeff value
                colname = corr_matrix.columns[i]  # getting the name of column
                col_corr.add(colname)
                # if colname in df.columns:
                #     del df[colname]
    return col_corr


def show_nan_statistics(df):
    print('\nNaN statistics:')
    # print column name and number of nan values in it if there are any
    shape = df.shape[0]
    printed_amount = 0
    for column in df.columns:
        nan_amount = df[column].isna().sum()
        if nan_amount > 0:
            print(f'{column}: {nan_amount} ({nan_amount / shape * 100:.2f}%)')
            printed_amount += 1
    if printed_amount == 0:
        print('No NaN values found')
    print('\n')


def save_first_n_rows(df, file_name, n=10):
    df.head(n).to_csv(f'./misc/{file_name}', index=False)


def print_current_dir():
    print(os.getcwd())


def print_columns_containing_string_values(df):
    print('\nColumns containing string values:')
    # for each column in df print column name and number of unique values in it
    columns = []
    for column in df.columns:
        if df[column].dtype == 'object':
            print(f'{column}: {len(df[column].unique())}')
            columns.append(column)
    print(f'\nDate difference: {df["date"].max()}, {df["date"].min()}')
    print('\n')
    return columns


# statistic for specific column: value and number of occurrences
# for example region column:
# Moscow - 1000
# Saint Petersburg - 500
def show_column_unique_values_stat(column):
    print(f'\n{column} unique values stat:')
    print(column.value_counts())


def add_new_features(df):
    print(f'\n{" Adding new features ":*^100}\n')

    df['is_moscow'] = df['region'].apply(lambda x: 1 if x == 'Москва' else 0)
    df['is_spb'] = df['region'].apply(lambda x: 1 if x == 'Санкт-Петербург' else 0)
    df['is_moscow_or_spb_oblast'] = \
        df['region'].apply(lambda x: 1 if x == 'Московская область' or x == 'Ленинградская область' else 0)
    df['is_krsk_oblast'] = df['region'].apply(lambda x: 1 if x == 'Краснодарский край' else 0)
    df['is_region'] = \
        df['region'].apply(
            lambda x: 1 if x not in ['Москва', 'Санкт-Петербург', 'Московская область', 'Ленинградская область',
                                     'Краснодарский край'] else 0
        )
    df.drop('region', axis=1, inplace=True)
    print(f'\nIs moscow: {df["is_moscow"].sum()}')
    print(f'Is spb: {df["is_spb"].sum()}')
    print(f'Is moscow or spb oblast: {df["is_moscow_or_spb_oblast"].sum()}')
    print(f'Is krsk oblast: {df["is_krsk_oblast"].sum()}')
    print(f'Is region: {df["is_region"].sum()}\n')

    cities_with_population_more_than_1_million = ['Москва', 'Санкт-Петербург', 'Новосибирск', 'Екатеринбург', 'Казань',
                                                  'Нижний Новгород', 'Челябинск', 'Самара', 'Омск',
                                                  'Ростов-на-Дону', 'Уфа', 'Красноярск', 'Воронеж', 'Пермь',
                                                  'Волгоград']
    # make column is_million (1 if city is in cities_with_population_more_than_1_million, 0 otherwise)
    df['is_million'] = df['city'].apply(lambda x: 1 if x in cities_with_population_more_than_1_million else 0)
    # make column is_not_million (1 if city is not in cities_with_population_more_than_1_million, 0 otherwise)
    df['is_not_million'] = df['city'].apply(lambda x: 1 if x not in cities_with_population_more_than_1_million else 0)
    # drop city column
    df.drop('city', axis=1, inplace=True)

    # covert date to datetime
    df['date'] = pd.to_datetime(df['date'])
    df['date'] = df['date'].apply(lambda x: x.timestamp())

    # get_dummies for city and region
    df = pd.get_dummies(df, columns=['osm_city_nearest_name', 'realty_type'])

    print(f'\n{" Adding new features finished ":*^100}\n')
    return df


def validate_data(df):
    print(f'\n{" Validating data... ":*^100}\n')
    show_nan_statistics(df)

    # remove column 'floor'
    df = df.drop(columns=['floor'])

    # fill nan_values is 'street' column with 'unknown'
    df['street'] = df['street'].fillna('unknown')

    # drop column 'osm_city_nearest_population'
    df = df.drop(columns=['osm_city_nearest_population'])

    # fill nan values in reform_house_population_1000 with 4
    df['reform_house_population_1000'] = df['reform_house_population_1000'].fillna(4)
    # fill nan values in reform_house_population_500 with average value

    # fill nan values in reform_mean_floor_count_1000 with 3
    df['reform_mean_floor_count_1000'] = df['reform_mean_floor_count_1000'].fillna(3)
    # fill nan values in reform_mean_floor_count_1000 with average value

    # fill nan values in reform_mean_year_building_1000 with 2000
    # TODO: find better way to fill nan values of reform_mean_year_building_1000
    df['reform_mean_year_building_1000'] = df['reform_mean_year_building_1000'].fillna(1970)
    # fill nan values in reform_mean_year_building_1000 with average value

    # fill nan values in reform_mean_year_building_1000 with average year around 5 km radius from house location

    # fill nan values in reform_house_population_500 with values from reform_house_population_1000
    df['reform_house_population_500'] = df['reform_house_population_500'].fillna(df['reform_house_population_1000'])
    # fill nan values in reform_house_population_500 with average value

    # fill nan values in reform_mean_floor_count_500 with values from reform_mean_floor_count_1000
    df['reform_mean_floor_count_500'] = df['reform_mean_floor_count_500'].fillna(df['reform_mean_floor_count_1000'])
    # fill nan values in reform_mean_floor_count_500 with average value

    # fill nan values in reform_mean_year_building_500 with values from reform_mean_year_building_1000
    df['reform_mean_year_building_500'] = df['reform_mean_year_building_500'].fillna(
        df['reform_mean_year_building_1000'])
    # fill nan values in reform_mean_year_building_500 with average value

    df = add_new_features(df)

    # show_statistics(df)
    show_nan_statistics(df)
    print(f'\n{" Data validation finished ":*^100}\n')
    return df


def show_column_isna_field_features(df, column, features):
    print(f'\nCity with nan {column} features: \n'
          f'{df[df[column].isna()][features].drop_duplicates()}')


def show_statistics(train_df):
    print(f'\n{" TABLE statistics ":-^100}')

    # print number of rows and columns in train_df
    print(f'\nNumber of rows: {train_df.shape[0]}')
    print(f'Number of columns: {train_df.shape[1]}')

    # print name of city which osm_city_nearest_population is nan
    # show_column_isna_field_features(train_df, 'osm_city_nearest_population', ['city', 'region'])

    # print name of city and latitude and longitude of it which reform_house_population_1000 is nan
    # show_column_isna_field_features(train_df, 'reform_house_population_1000', ['region'])

    # print name of city and latitude and longitude of it which reform_mean_floor_count_1000 is nan
    # show_column_isna_field_features(train_df, 'reform_mean_floor_count_1000', ['region'])

    # print name of city which reform_mean_year_building_1000 is nan
    # show_column_isna_field_features(train_df, 'reform_mean_year_building_1000', ['region'])

    # show_column_unique_values_stat(train_df['region'])

    string_columns = print_columns_containing_string_values(train_df)
    # print delimeter sign
    print('-' * 100)
    return string_columns


def show_train_statistics(train):
    # average 'per_square_meter_price' for each 'region'
    print(f'\n{" Average per_square_meter_price for each region ":-^100}')
    print(train.groupby('region')['per_square_meter_price'].mean())


def statistics(predicted_price, test):
    # make column 'predicted_price' in test
    test['predicted_price'] = predicted_price

    # show regions which have 'predicted_price' more van 200000 and less than 300000
    print(f'\n{" Regions which have predicted_price more than 200000 and less than 300000 ":-^100}')
    print(test[(test['predicted_price'] > 200000) & (test['predicted_price'] < 300000)]['region'].unique())


def main():
    print_current_dir()

    predict_target = 'per_square_meter_price'

    # read data/train.csv using pandas
    train = pd.read_csv('./data/train.csv')

    show_train_statistics(train)

    # read test_df
    test = pd.read_csv('./data/test_x.csv')

    test_copy = test.copy()

    target = train[predict_target]
    train.drop(columns=[predict_target], axis=1, inplace=True)

    # concat train and test
    df = pd.concat([train, test], axis=0)

    show_statistics(df)

    df = validate_data(df)

    save_first_n_rows(df, 'df.csv', 100)

    string_values = show_statistics(df)

    df.drop(columns=string_values, axis=1, inplace=True)

    print(f'\n{" Predicting... ":.^100}\n')

    # create test_x and test_y datasets
    train = df.iloc[:len(target)]
    test = df.iloc[len(target):]

    drop1 = analyze_variance(train, target)
    df = df.drop(columns=drop1)

    save_first_n_rows(df, 'df.csv', 100)

    drop2 = list(correlation(df, 0.7))
    print(drop2)
    # drop from df drop2
    df = df.drop(columns=drop2)

    save_first_n_rows(df, 'df.csv', 100)

    train = df.iloc[:len(target)]
    test = df.iloc[len(target):]
    # model
    model = LinearRegression()
    model.fit(train, target)

    # predict
    predicted_price = model.predict(test)

    statistics(predicted_price, test_copy)

    # create dataframe with predicted price
    predicted_price_df = pd.DataFrame({'id': range(len(predicted_price)), predict_target: predicted_price})

    # save predicted price to csv file
    predicted_price_df.to_csv('./res/predicted_price.csv', index=False)

    # plot predicted price and real price in histogram plot
    # divide each bin size with amount of data
    # remove outliers from predicted price and real price

    # randomly choose data from target with size of predicted_price
    # target = target.sample(n=len(predicted_price))
    target = target[:len(predicted_price)]

    # log transform predicted_price and target
    # predicted_price = np.log(predicted_price)
    # target = np.log(target)

    # fix intervals of bins in histogram plot
    min_val = min(min(predicted_price), min(target))
    max_val = max(max(predicted_price), max(target))

    # mor lables in x axis in histogram plot

    # plt.xticks(np.arange(min(predicted_price), max(predicted_price) + 1, 1e6))
    plt.hist(predicted_price, bins=200, range=(min_val, max_val), alpha=0.5, label='predicted price')
    plt.hist(target, bins=200, range=(min_val, max_val), alpha=0.5, label='real price')
    plt.legend(loc='upper right')
    # set time to plot title
    plt.title(f'Predicted price and real price histogram plot at {time.strftime("%H:%M:%S")}')
    plt.show()

    print(f'\n{" Predicted ":.^100}\n')


if __name__ == '__main__':
    main()
