import os
from math import radians, sin, cos, sqrt, atan2

import pandas as pd
from sklearn.linear_model import LinearRegression
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

    print(df.corr())

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

    # read test_x dataset from csv file
    test_df = pd.read_csv('./data/test_x.csv')

    # create test_x dataset
    test_x = test_df

    # validate model
    test_x = validate_data(test_x)

    test_x = test_x.drop(columns=string_columns)

    # predict price
    predicted_price = model.predict(test_x)

    # create dataframe with predicted price
    predicted_price_df = pd.DataFrame({target: predicted_price, 'id': test_df.index})

    # save predicted price to csv file
    predicted_price_df.to_csv('./res/predicted_price.csv', index=False)

    print(f'\n{" Model trained ":.^100}\n')


def main():
    # print current dir
    print_current_dir()

    # read data/train.csv using pandas
    train_df = pd.read_csv('./data/train.csv')

    string_columns = show_statistics(train_df)

    save_first_n_rows(train_df, 200)

    train_df = validate_data(train_df)

    train(train_df, string_columns)


if __name__ == '__main__':
    main()
