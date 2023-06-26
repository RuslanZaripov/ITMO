import os
import time

import numpy as np
import pandas as pd
from matplotlib import pyplot as plt
from numpy import Infinity
from sklearn.linear_model import LinearRegression
from sklearn.metrics import r2_score, mean_absolute_percentage_error

# https://www.kaggle.com/datasets/lildatascientist/raifhackds2021fall

# reform_house_population_1000 - Coefficient of the number of people living within a radius of 1 km by source Reform
# reform_mean_floor_count_1000 - Average number of storeys of houses within a radius of 1 km according to the source
# reform_mean_year_building_1000 - Average value of the year of construction of houses within a radius of 1 km

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


def print_features_variance(df):
    for feature in df.columns:
        print(f"{f'{feature}:':<35}{df[feature].var():>25}", )


def analyze_variance(train, target):
    print(f'\n{" Analyzing Features variance ":.^100}\n')

    print_features_variance(train)

    best_error = Infinity
    best_var = 0
    best_features = []
    best_features_to_drop = []
    errors = []
    variances = []
    # vals = [0.001, 0.003, 0.005, 0.1]
    vals = [0, 0.00001, 0.00005, 0.0001, 0.0005, 0.001, 0.005, 0.01, 0.1]
    # vals = [0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1]
    # vals = [0.01]
    for var in vals:
        features = train.var()[train.var() > var].index.tolist()
        fearures_to_drop = train.var()[train.var() <= var].index.tolist()

        train_x = train[features]
        train_y = target

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

    print(f'\n{" Analyzing Features variance finished ":.^100}\n')
    return best_features_to_drop


def correlation(df, threshold):
    col_corr = set()
    corr_matrix = df.corr()
    for i in range(len(corr_matrix.columns)):
        for j in range(i):
            if abs(corr_matrix.iloc[i, j]) > threshold:
                colname = corr_matrix.columns[i]
                col_corr.add(colname)
    return col_corr


def show_nan_statistics(df):
    print('\nNaN statistics:')
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
    columns = []
    for column in df.columns:
        if df[column].dtype == 'object':
            print(f'{column}: {len(df[column].unique())}')
            columns.append(column)
    print(f'\nDate difference: {df["date"].max()}, {df["date"].min()}')
    print(f'Total square difference: {df["total_square"].max()}, {df["total_square"].min()}')
    print('\n')
    return columns


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
    df['is_million'] = df['city'].apply(lambda x: 1 if x in cities_with_population_more_than_1_million else 0)
    df['is_not_million'] = df['city'].apply(lambda x: 1 if x not in cities_with_population_more_than_1_million else 0)
    df.drop('city', axis=1, inplace=True)
    print(f'\nIs million: {df["is_million"].sum()}')
    print(f'Is not million: {df["is_not_million"].sum()}\n')

    df['date'] = pd.to_datetime(df['date'], format='%Y-%m-%d')
    df['year'] = df['date'].dt.year
    df['month'] = df['date'].dt.month

    df['date'] = scale_column_min_max(df['date'])

    df['total_square'] = scale_column_min_max(df['total_square'])

    df = pd.get_dummies(df, columns=['osm_city_nearest_name', 'realty_type'])

    print(f'\n{" Adding new features finished ":*^100}\n')
    return df


def scale_column(column):
    return (column - column.mean()) / column.std()


def scale_column_min_max(column):
    return (column - column.min()) / (column.max() - column.min())


def validate_data(df):
    print(f'\n{" Validating data... ":*^100}\n')
    show_nan_statistics(df)

    df['street'] = df['street'].fillna('unknown')

    df['osm_city_nearest_population'] = df['osm_city_nearest_population'].fillna(df['city'].map(missing_population))

    df['missing_reform_stat'] = df['reform_house_population_1000'].apply(lambda x: 1 if pd.isna(x) else 0)
    df['reform_house_population_1000'] = df['reform_house_population_1000'].fillna(4)

    df['missing_reform_stat'] = df['reform_mean_floor_count_1000'].apply(lambda x: 1 if pd.isna(x) else 0)
    df['reform_mean_floor_count_1000'] = df['reform_mean_floor_count_1000'].fillna(3)

    df['missing_reform_stat'] = df['reform_mean_year_building_1000'].apply(lambda x: 1 if pd.isna(x) else 0)
    df['reform_mean_year_building_1000'] = df['reform_mean_year_building_1000'].fillna(1970)

    df['reform_house_population_500'] = df['reform_house_population_500'].fillna(
        df['reform_house_population_1000']
    )

    df['reform_mean_floor_count_500'] = df['reform_mean_floor_count_500'].fillna(
        df['reform_mean_floor_count_1000']
    )

    df['reform_mean_year_building_500'] = df['reform_mean_year_building_500'].fillna(
        df['reform_mean_year_building_1000']
    )

    df['floor'] = df['floor'].fillna(df['reform_mean_floor_count_1000'])

    df['total_square_bin'] = pd.qcut(df['total_square'], q=10, labels=False)

    df['floor_bin'] = pd.qcut(df['floor'], q=2, labels=False)

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

    print(f'\nNumber of rows: {train_df.shape[0]}')
    print(f'Number of columns: {train_df.shape[1]}')

    # show_column_isna_field_features(train_df, 'osm_city_nearest_population', ['city', 'region'])

    # show_column_isna_field_features(train_df, 'reform_house_population_1000', ['region'])

    # show_column_isna_field_features(train_df, 'reform_mean_floor_count_1000', ['region'])

    # show_column_isna_field_features(train_df, 'reform_mean_year_building_1000', ['region'])

    # show_column_unique_values_stat(train_df['region'])

    string_columns = print_columns_containing_string_values(train_df)

    # unique_values_amount(string_columns, train_df)

    print('-' * 100)
    return string_columns


def unique_values_amount(string_columns, train_df):
    print(f'\nNumber of unique values in each column:')
    for column in train_df.columns:
        if column not in string_columns:
            print(f'{column}: {train_df[column].nunique()}')


def show_train_statistics(train):
    print(f'\n{" Average per_square_meter_price for each region ":-^100}')
    print(train.groupby('region')['per_square_meter_price'].mean())


def statistics(predicted_price, test):
    test['predicted_price'] = predicted_price

    print(f'\n{" Regions which have predicted_price more than 200000 and less than 1000000 ":-^100}')
    print(test[(test['predicted_price'] > 200000) & (test['predicted_price'] < 1000000)]['region'].unique())

    print(f'\n{" Min and max total_squares which have predicted_price more than 200000 and less than 1000000 ":-^100}')
    print(test[(test['predicted_price'] > 200000) & (test['predicted_price'] < 1000000)][
              ['total_square', 'predicted_price']].min())
    print(test[(test['predicted_price'] > 200000) & (test['predicted_price'] < 1000000)][
              ['total_square', 'predicted_price']].max())


def analyze_correlation(train, target):
    print(f'\n{" Correlation between features and target ":-^100}')

    vals = [0.4, 0.6, 0.7, 0.8, 0.9, 0.95, 0.96, 0.97]
    best_val = 0
    best_error = Infinity
    errors = []
    best_features_to_drop = []
    correlations = []
    for val in vals:
        drop = correlation(train, val)

        test_x = train.drop(columns=drop)
        target_y = target

        model = LinearRegression()
        model.fit(test_x, target_y)

        predict_y = model.predict(test_x)

        r2 = r2_score(target_y, predict_y)
        print(f'Correlation {val}: {r2}')

        error = mean_absolute_percentage_error(target_y, predict_y)
        print(f'MAPE {val}: {error}')

        if error < best_error:
            best_error = error
            best_val = val
            best_features_to_drop = drop
        errors.append(error)
        correlations.append(val)

    print(f'\nBest features stat: {best_val} {best_error} - {best_features_to_drop}')

    plt.plot(correlations, errors)
    plt.xlabel('Correlation')
    plt.ylabel('MAPE')
    plt.show()

    print(f'\n{" Analyzing Features correlation ":.^100}\n')
    return best_features_to_drop


def correlation_with_target(train, target):
    print(f'\n{" Correlation between features and target ":-^100}')
    train.corrwith(target).to_csv('./misc/correlation_with_target.csv', header=True)

    drop = train.corrwith(target).abs() < 0.01

    drop = list(drop[drop].index)

    print(f'\n{" Correlation between features and target ended ":.^100}\n')
    return drop


def replace_outliers_in_column(param):
    param = param.copy()
    q1 = param.quantile(0.05)
    q3 = param.quantile(0.95)
    iqr = q3 - q1
    lower_bound = q1 - 1.5 * iqr
    upper_bound = q3 + 1.5 * iqr
    param.loc[param < lower_bound] = np.nan
    param.loc[param > upper_bound] = np.nan
    param.fillna(param.mean(), inplace=True)
    return param


def replace_outliers_train(train):
    print(f'\n{" Replacing outliers for per_square_meter_price ":.^100}')

    train['per_square_meter_price'] = train.groupby('region')['per_square_meter_price'].apply(
        lambda x: x.clip(lower=x.quantile(0.05), upper=x.quantile(0.95)))

    train['per_square_meter_price'] = train.groupby('osm_city_nearest_name')['per_square_meter_price'].apply(
        lambda x: x.clip(lower=x.quantile(0.05), upper=x.quantile(0.95)))

    train['per_square_meter_price'] = train.groupby('total_square')['per_square_meter_price'].apply(
        lambda x: x.clip(lower=x.quantile(0.05), upper=x.quantile(0.95)))

    print(f'\n{" Replacing outliers for per_square_meter_price ended ":.^100}\n')
    return train


def replace_outliers(df):
    print(f'\n{" Removing outliers ":.^100}')

    for column in df.columns:
        if column.startswith('osm_') and df[column].dtype != 'object':
            df[column] = df[column].clip(lower=df[column].quantile(0.05), upper=df[column].quantile(0.95))

    for column in df.columns:
        if column.startswith('reform_') and df[column].dtype != 'object':
            df[column] = df[column].clip(lower=df[column].quantile(0.05), upper=df[column].quantile(0.95))

    df['total_square'] = df['total_square'].clip(lower=df['total_square'].quantile(0.05),
                                                 upper=df['total_square'].quantile(0.95))

    for column in df.columns:
        if column.startswith('osm_') and df[column].dtype != 'object':
            df[column] = np.log1p(df[column])

    for column in df.columns:
        if column.startswith('reform_') and df[column].dtype != 'object':
            df[column] = np.log1p(df[column])

    print(f'\n{" Removing outliers ended ":.^100}\n')
    return df


def low_variance_stat(train):
    print(f'\n{" Low correlation columns ":.^100}')
    for column in train.columns:
        if column != 'per_square_meter_price':
            if train[column].dtype != 'object':
                print(f'{column} correlation with target: {train[column].corr(train["per_square_meter_price"])}')

    print(f'\n{" Low correlation columns ended ":.^100}\n')


def main():
    print_current_dir()

    predict_target = 'per_square_meter_price'

    train = pd.read_csv('./data/train.csv')

    train = replace_outliers_train(train)

    low_variance_stat(train)

    test = pd.read_csv('./data/test_x.csv')

    test_copy = test.copy()

    target = train[predict_target]
    train.drop(columns=[predict_target], axis=1, inplace=True)

    df = pd.concat([train, test], axis=0)

    show_statistics(df)

    # df.describe().round(2).T.to_csv('./misc/describe_before.csv', header=True)

    df = validate_data(df)

    df = replace_outliers(df)

    # df.describe().round(2).T.to_csv('./misc/describe_after.csv', header=True)

    # save_first_n_rows(df, 'df.csv', 100)

    string_values = show_statistics(df)

    df.drop(columns=string_values, axis=1, inplace=True)

    print(f'\n{" Predicting... ":.^100}\n')

    train = df.iloc[:len(target)]
    test = df.iloc[len(target):]

    drop1 = analyze_variance(train, target)
    df = df.drop(columns=drop1)

    # save_first_n_rows(df, 'df.csv', 100)

    # drop3 = analyze_correlation(train, target)  # testing
    drop2 = list(correlation(df, 0.8))
    print(drop2)
    df = df.drop(columns=drop2)

    # train = df.iloc[:len(target)]
    # test = df.iloc[len(target):]
    # drop3 = correlation_with_target(train, target)
    # print(drop3)
    # df = df.drop(columns=drop3)

    # save_first_n_rows(df, 'df.csv', 100)

    train = df.iloc[:len(target)]
    test = df.iloc[len(target):]

    model = LinearRegression()
    model.fit(train, target)

    predicted_price = model.predict(test)

    statistics(predicted_price, test_copy)

    predicted_price_df = pd.DataFrame({'id': range(len(predicted_price)), predict_target: predicted_price})

    predicted_price_df[predict_target] = predicted_price_df[predict_target].clip(lower=1)

    predicted_price_df.to_csv('./res/predicted_price.csv', index=False)

    # hist(predicted_price, target)

    print(f'\n{" Predicted ":.^100}\n')


def hist(predicted_price, target):
    predicted_price = np.log1p(predicted_price - predicted_price.min() + 1)
    target = np.log1p(target)
    min_val = min(min(predicted_price), min(target))
    max_val = max(max(predicted_price), max(target))
    plt.hist(predicted_price, bins=100, range=(min_val, max_val), alpha=0.5, label='predicted price')
    plt.hist(target, bins=100, range=(min_val, max_val), alpha=0.5, label='real price')
    plt.legend(loc='upper right')
    plt.title(f'Predicted price and real price histogram plot at {time.strftime("%H:%M:%S")}')
    plt.show()


if __name__ == '__main__':
    main()
