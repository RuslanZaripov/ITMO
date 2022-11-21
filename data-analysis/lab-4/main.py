import os
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

missing_population = {
    'Лесозаводск': 35433,
    'Новопокровка': 3095,
    'Восток': 3313,
    'Дальнегорск': 33655,
    'Дальнереченск': 23613,
    'Пожарский район, с. Светлогорье, Первый мкр': 1396,
    'Пластун': 4815,
    'Лучегорск': 17437,
    'Пожарский район, Лучегорск пгт': 17437,
    'Кировский': 8033,
    'Кировский район, Кировский пгт': 8033,
    'Горные Ключи': 4302,
    'Кировский район, Горные Ключи кп': 4302,
}


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


def save_first_n_rows(df, n=10):
    df.head(n).to_csv('./misc/part.csv', index=False)


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


def add_new_features(df):
    print(f'\n{" Add new features ":*^100}\n')

    # divide column region into 3 columns:
    # is_moscow (1 if region is 'Москва', 0 otherwise),
    # is_spb (1 if region is 'Санкт-Петербург', 0 otherwise),
    # is_moscow_oblast (1 if region is 'Московская область', 0 otherwise)
    # is_region (1 if region is not 'Москва', 'Санкт-Петербург', 'Московская область', 0 otherwise)
    df['is_moscow'] = df['region'].apply(lambda x: 1 if x == 'Москва' else 0)
    df['is_spb'] = df['region'].apply(lambda x: 1 if x == 'Санкт-Петербург' else 0)
    df['is_moscow_oblast'] = df['region'].apply(lambda x: 1 if x == 'Московская область' else 0)
    df['is_kazan'] = df['region'].apply(lambda x: 1 if x == 'Казань' else 0)
    df['is_region'] = \
        df['region'].apply(lambda x: 1 if x not in ['Москва', 'Санкт-Петербург', 'Московская область', 'Казань'] else 0)

    # drop region column
    df.drop('region', axis=1, inplace=True)

    # print how many 1s and 0s are in cloumns is_moscow, is_spb, is_moscow_oblast, is_region, is_kazan
    print(f'\nIs moscow: {df["is_moscow"].sum()}')
    print(f'Is spb: {df["is_spb"].sum()}')
    print(f'Is kazan: {df["is_kazan"].sum()}')
    print(f'Is moscow oblast: {df["is_moscow_oblast"].sum()}')
    print(f'Is region: {df["is_region"].sum()}\n')

    print(f'\n{" Add new features finished ":*^100}\n')
    return df


def validate_data(df):
    print(f'\n{" Validating data... ":*^100}\n')
    # count nan fields
    show_nan_statistics(df)

    # remove column 'floor'
    df = df.drop(columns=['floor'])

    # fill nan_values is 'street' column with 'unknown'
    df['street'] = df['street'].fillna('unknown')

    # sort train_df by date column
    df = df.sort_values(by=['date'])

    # fill nan values in osm_city_nearest_population with values from missing_population dict
    df['osm_city_nearest_population'] = df['osm_city_nearest_population'].fillna(df['city'].map(missing_population))

    # fill nan values in reform_house_population_1000 with 4
    df['reform_house_population_1000'] = df['reform_house_population_1000'].fillna(4)

    # fill nan values in reform_mean_floor_count_1000 with 3
    df['reform_mean_floor_count_1000'] = df['reform_mean_floor_count_1000'].fillna(3)

    # fill nan values in reform_mean_year_building_1000 with 2000
    # TODO: find better way to fill nan values of reform_mean_year_building_1000
    df['reform_mean_year_building_1000'] = df['reform_mean_year_building_1000'].fillna(1970)

    # fill nan values in reform_mean_year_building_1000 with average year around 5 km radius from house location

    # fill nan values in reform_house_population_500 with values from reform_house_population_1000
    df['reform_house_population_500'] = df['reform_house_population_500'].fillna(df['reform_house_population_1000'])

    # fill nan values in reform_mean_floor_count_500 with values from reform_mean_floor_count_1000
    df['reform_mean_floor_count_500'] = df['reform_mean_floor_count_500'].fillna(df['reform_mean_floor_count_1000'])

    # fill nan values in reform_mean_year_building_500 with values from reform_mean_year_building_1000
    df['reform_mean_year_building_500'] = df['reform_mean_year_building_500'].fillna(
        df['reform_mean_year_building_1000'])

    # df = add_new_features(df)

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
    show_column_isna_field_features(train_df, 'osm_city_nearest_population', ['city', 'region'])

    # print name of city and latitude and longitude of it which reform_house_population_1000 is nan
    # show_column_isna_field_features(train_df, 'reform_house_population_1000', ['region'])

    # print name of city and latitude and longitude of it which reform_mean_floor_count_1000 is nan
    # show_column_isna_field_features(train_df, 'reform_mean_floor_count_1000', ['region'])

    # print name of city which reform_mean_year_building_1000 is nan
    # show_column_isna_field_features(train_df, 'reform_mean_year_building_1000', ['region'])

    string_columns = print_columns_containing_string_values(train_df)
    # print delimeter sign
    print('-' * 100)
    return string_columns


# count distance in km between two points on the Earth
# TODO: maybe use this method to predict values
def count_distance(lat1, lng1, lat2, lng2):
    print(f'lat1: {lat1}, lng1: {lng1}, lat2: {lat2}, lng2: {lng2}')

    # approximate radius of earth in km
    R = 6373.0

    lat1 = radians(lat1)
    lng1 = radians(lng1)
    lat2 = radians(lat2)
    lng2 = radians(lng2)

    dlng = lng2 - lng1
    dlat = lat2 - lat1

    a = sin(dlat / 2) ** 2 + cos(lat1) * cos(lat2) * sin(dlng / 2) ** 2
    c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return R * c


# Need to predict cost per square meter using LinearRegression from sklearn python package. You cannot use other
# models in the solution Apply the train-trained model to the test_x dataset to predict the price from test_x. Write
# the prediction to a file in the following format The solution file should have 2 columns: row ID and model prediction
def train(train_df, string_columns):
    print(f'\n{" Training model... ":.^100}\n')

    # create LinearRegression model
    model = LinearRegression()

    target = 'per_square_meter_price'
    # create new list object of string_columns with target column
    string_columns_with_target = string_columns + [target]

    print(f'\nColumns with string values: {string_columns}')
    print(f'\nColumns with string values and target: {string_columns_with_target}')

    # create train_x and train_y datasets
    train_x = train_df.drop(columns=string_columns_with_target)
    train_y = train_df[target]

    # train model
    model.fit(train_x, train_y)

    print(f'\n{" Model trained ":.^100}\n')
    return model


def visualize_corr(X_train):
    # create correlation matrix
    # corr_matrix = X_train.corr().abs()

    # select upper triangle of correlation matrix
    # upper = corr_matrix.where(np.triu(np.ones(corr_matrix.shape), k=1).astype(np.bool))

    # find index of feature columns with correlation greater than 0.95
    # to_drop = [column for column in upper.columns if any(upper[column] > 0.95)]

    # drop features
    # X_train.drop(X_train[to_drop], axis=1)

    # plot correlation matrix
    plt.figure(figsize=(20, 20))
    sns.heatmap(X_train.corr(), annot=True, fmt='.2f', cmap='coolwarm')
    plt.show()


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


def train2(train_df, target, features_with_target, string_columns):
    print(f'\n{" Training model... ":.^100}\n')

    # try to avoid overfitting
    # X_train, X_test, y_train, y_test = train_test_split(
    #     train_df.drop(columns=string_columns_with_target),
    #     train_df[target],
    #     test_size=0.2,
    #     random_state=42
    # )

    # create train_x and train_y datasets
    X_train = train_df.drop(columns=features_with_target)
    y_train = train_df[target]

    # visualize_corr(X_train)
    # print("high correltaed values: ", correlation(X_train, 0.7))
    show_features_variance(X_train)
    best_features_var = analyze_variance(X_train, y_train)
    best_features_corr = analyze_correltaion(train_df.drop(columns=string_columns), target)

    # intersect best features
    best_features = list(set(best_features_var) & set(best_features_corr))
    print(f'\nBest features: {best_features}')

    # create LinearRegression model
    model = LinearRegression()

    # train model
    model.fit(X_train[best_features], y_train)

    print(f'\n{" Model trained ":.^100}\n')
    return model, best_features


def show_features_variance(df):
    print(f'\n{" Features variance ":.^100}\n')

    max_len = max([len(feature) for feature in df.columns])
    for feature in df.columns:
        var = df[feature].var()
        print(f"{f'{feature}:':<{max_len}} {f'{var:.5f}':>15}")

    print(f'\n{" Features variance ":.^100}\n')


def analyze_variance(df_train, df_test):
    print(f'\n{" Analyzing Features variance ":.^100}\n')
    best_error = Infinity
    best_features = []
    errors = []
    variances = []
    # best var 2
    for var in [2, 2.1]:
        features = df_train.var()[df_train.var() > var].index.tolist()

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
        errors.append(error)
        variances.append(var)

    print(f'\nBest features stat: {best_error} - \n{best_features}')

    plt.plot(variances, errors)
    plt.xlabel('Variance')
    plt.ylabel('MAPE')
    plt.show()

    print(f'\n{" Analyzing Features variance ":.^100}\n')
    return best_features


def analyze_correltaion(df_train, predict_target):
    print(f'\n{" Analyzing Features correlation ":.^100}\n')

    print(df_train[predict_target])

    # best val 0,15
    # vals = [0.03, 0.14, 0.15, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9]
    vals = [0.15]
    best_features = []
    best_error = Infinity
    errors = []
    correlations = []
    for val in vals:
        features = df_train.corr()[predict_target][df_train.corr()[predict_target] > val].index.tolist()

        if len(features) == 0:
            continue

        train_x = df_train[features]
        train_y = df_train[predict_target]

        model = LinearRegression()
        model.fit(train_x, train_y)
        y_pred = model.predict(train_x)

        r2 = r2_score(train_y, y_pred)
        print(f'Correlation {val}: {r2}')

        error = mean_absolute_percentage_error(train_y, y_pred)
        print(f'MAPE {val}: {error}')

        if error < best_error:
            best_error = error
            best_features = features
        errors.append(error)
        correlations.append(val)

    print(f'Best features {best_error}: {best_features}')

    plt.plot(correlations, errors)
    plt.xlabel('Correlation')
    plt.ylabel('MAPE')
    plt.show()

    print(f'\n{" Analyzing Features correlation ":.^100}\n')
    return best_features


def r2_score_stat(predicted_price, y_test):
    # calculate r2 score
    r2 = r2_score(y_test, predicted_price)
    # print(f'\nR2 score: {r2}')
    return r2


def mape_stat(predicted_price, y_test):
    # calculate mean absolute error
    mape = mean_absolute_percentage_error(y_test, predicted_price)
    # print(f'\nMean absolute percentage error: {mape}')
    return mape


def predict(model, target, features_to_drop, best_features):
    print(f'\n{" Predicting... ":.^100}\n')

    # read test_x dataset from csv file
    test_df = pd.read_csv('./data/test_x.csv')

    # create test_x dataset
    test_x = test_df

    # validate model
    test_x = validate_data(test_x)

    test_x = test_x.drop(columns=features_to_drop)

    # predict price
    predicted_price = model.predict(test_x[best_features])

    # create dataframe with predicted price
    predicted_price_df = pd.DataFrame({target: predicted_price, 'id': test_df.index})

    # save predicted price to csv file
    predicted_price_df.to_csv('./res/predicted_price.csv', index=False)

    print(f'\n{" Predicted ":.^100}\n')


def main():
    # print current dir
    print_current_dir()

    # read data/train.csv using pandas
    train_df = pd.read_csv('./data/train.csv')

    string_columns = show_statistics(train_df)

    save_first_n_rows(train_df, 200)

    train_df = validate_data(train_df)

    target = 'per_square_meter_price'
    # create new list object of string_columns with target column
    string_columns_with_target = string_columns + [target]

    print(f'\nColumns with string values: {string_columns}')
    print(f'\nColumns with string values and target: {string_columns_with_target}')

    model, best_features = train2(train_df, target, string_columns_with_target, string_columns)

    predict(model, target, string_columns, best_features)


if __name__ == '__main__':
    main()
