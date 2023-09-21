# Status of existing checking account, in Deutsche Mark.
# Duration in months
# Credit history (credits taken, paid back duly, delays, critical accounts)
# Purpose of the credit (car, television,...)
# Credit amount
# Status of savings account/bonds, in Deutsche Mark.
# Present employment, in number of years.
# Installment rate in percentage of disposable income
# Personal status (married, single,...) and sex
# Other debtors / guarantors
# Present residence since X years
# Property (e.g. real estate)
# Age in years
# Other installment plans (banks, stores)
# Housing (rent, own,...)
# Number of existing credits at this bank
# Job
# Number of people being liable to provide maintenance for
# Telephone (yes,no)
# Foreign worker (yes,no)

import numpy as np
import pandas as pd
from matplotlib import pyplot as plt
from optuna.pruners import SuccessiveHalvingPruner
from scipy.stats import uniform
from sklearn.ensemble import RandomForestClassifier

import optuna
from sklearn.neighbors import KNeighborsClassifier
from sklearn.svm import SVC
from sklearn.naive_bayes import GaussianNB
from sklearn.tree import DecisionTreeClassifier

from sklearn.model_selection import RandomizedSearchCV
from sklearn.metrics import roc_curve
from sklearn.metrics import roc_auc_score

from sklearn.metrics import confusion_matrix
import seaborn as sns

from sklearn.preprocessing import StandardScaler

from sklearn.model_selection import train_test_split

import optuna.visualization as optvis


def main(name):
    df = pd.read_csv('data/credit-g.csv')

    df.columns = df.columns.str.replace("'", '')

    categorical_columns = df.select_dtypes(include=['object']).columns
    numerical_columns = df.select_dtypes(include=['int64']).columns

    my_print(df.isnull().sum().sum())

    for col in categorical_columns:
        df[col] = df[col].astype('category')
        df[col] = df[col].cat.codes

    columns_with_outliers = ['age', 'duration', 'credit_amount']


    # for col in columns_with_outliers:
    #     plt.figure()
    #     df.boxplot([col])
    #     plt.title(col)
    #     plt.savefig('images/boxplot_' + col + '.png')

    # for col in columns_with_outliers:
    #     plt.figure()
    #     df[col].hist()
    #     plt.title(col)
    #     plt.savefig('images/histogram_' + col + '.png')

    for col in columns_with_outliers:
        q1 = df[col].quantile(0.15)
        q3 = df[col].quantile(0.85)
        iqr = q3 - q1
        lower_bound = q1 - 1.5 * iqr
        upper_bound = q3 + 1.5 * iqr
        df = df[(df[col] > lower_bound) & (df[col] < upper_bound)]

    # for col in columns_with_outliers:
    #     plt.figure()
    #     df.boxplot([col])
    #     plt.title(col)
    #     plt.title(col + ' without outliers')
    #     plt.savefig('images/boxplot_' + col + '_without_outliers.png')

    scaler = StandardScaler()
    df[numerical_columns] = scaler.fit_transform(df[numerical_columns])

    X = df.drop('class', axis=1)
    y = df['class']

    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
    X_train, X_val, y_train, y_val = train_test_split(X_train, y_train, test_size=0.2, random_state=42)

    optuna_method(X_train, y_train, X_val, y_val, X_test, y_test)
    random_search_method(X_train, y_train, X_test, y_test)


def optuna_method(X_train, y_train, X_val, y_val, X_test, y_test):
    def objective(m_trial):
        m_classifier_obj = None
        m_classifier_name = m_trial.suggest_categorical('classifier', ['KNN', 'SVC', 'Bayes', 'Tree', 'RandomForest'])

        if m_classifier_name == 'KNN':
            m_n_neighbors = m_trial.suggest_int('n_neighbors', 1, 15)
            m_weight = m_trial.suggest_categorical('weight', ['uniform', 'distance'])
            m_algorithm = m_trial.suggest_categorical('algorithm', ['ball_tree', 'kd_tree', 'brute'])
            m_metric = m_trial.suggest_categorical('metric', ['euclidean', 'manhattan', 'chebyshev', 'minkowski'])
            m_leaf_size = m_trial.suggest_int('leaf_size', 1, 15)
            m_p = m_trial.suggest_int('p', 1, 2)

            m_classifier_obj = KNeighborsClassifier(
                n_neighbors=m_n_neighbors,
                weights=m_weight,
                algorithm=m_algorithm,
                metric=m_metric,
                leaf_size=m_leaf_size,
                p=m_p,
                n_jobs=-1
            )
        elif m_classifier_name == 'SVC':
            m_svc_c = m_trial.suggest_float('svc_c', 1e-3, 1e3)
            m_kernel = m_trial.suggest_categorical('kernel', ['linear', 'poly', 'rbf', 'sigmoid'])

            m_classifier_obj = SVC(C=m_svc_c, kernel=m_kernel)
        elif m_classifier_name == 'Bayes':
            m_var_smoothing = m_trial.suggest_float('var_smoothing', 1e-10, 1)

            m_classifier_obj = GaussianNB(
                var_smoothing=m_var_smoothing
            )
        elif m_classifier_name == 'Tree':
            m_criterion = m_trial.suggest_categorical('criterion', ['gini', 'entropy'])
            m_splitter = m_trial.suggest_categorical('splitter', ['best', 'random'])
            m_max_depth = m_trial.suggest_int('max_depth', 1, 32)
            m_min_samples_split = m_trial.suggest_int('min_samples_split', 2, 10)
            m_min_samples_leaf = m_trial.suggest_int('min_samples_leaf', 1, 10)

            m_classifier_obj = DecisionTreeClassifier(
                max_depth=m_max_depth,
                criterion=m_criterion,
                splitter=m_splitter,
                min_samples_split=m_min_samples_split,
                min_samples_leaf=m_min_samples_leaf,
            )
        elif m_classifier_name == 'RandomForest':
            m_max_depth = m_trial.suggest_int('max_depth', 2, 32)
            m_n_estimators = m_trial.suggest_int('n_estimators', 1, 100)
            m_max_features = m_trial.suggest_categorical('max_features', ['sqrt', 'log2'])
            m_criterion = m_trial.suggest_categorical('criterion', ['gini', 'entropy'])
            m_min_samples_split = m_trial.suggest_int('min_samples_split', 2, 10)

            m_classifier_obj = RandomForestClassifier(
                max_depth=m_max_depth,
                n_estimators=m_n_estimators,
                max_features=m_max_features,
                criterion=m_criterion,
                min_samples_split=m_min_samples_split,
                n_jobs=-1
            )

        m_classifier_obj.fit(X_train, y_train)
        return m_classifier_obj.score(X_val, y_val)

    study = optuna.create_study(
        direction='maximize',
        sampler=optuna.samplers.TPESampler(seed=42),
        load_if_exists=True,
        pruner=SuccessiveHalvingPruner()
    )

    study.optimize(
        objective,
        timeout=600,
        callbacks=[optuna.study.MaxTrialsCallback(
            100,
            states=(optuna.trial.TrialState.COMPLETE, optuna.trial.TrialState.PRUNED))
        ],
        n_jobs=-1,
        gc_after_trial=True
    )

    print('Number of finished trials:', len(study.trials))

    print('Best trial:')
    trial = study.best_trial

    print('  Value: {}'.format(trial.value))

    print('  Params: ')
    for key, value in trial.params.items():
        print('    {}: {}'.format(key, value))

    classifier_obj = None
    classifier_name = trial.params['classifier']

    if classifier_name == 'KNN':
        n_neighbors = trial.params['n_neighbors']
        weight = trial.params['weight']
        algorithm = trial.params['algorithm']
        metric = trial.params['metric']
        leaf_size = trial.params['leaf_size']
        p = trial.params['p']

        classifier_obj = KNeighborsClassifier(
            n_neighbors=n_neighbors,
            weights=weight,
            algorithm=algorithm,
            metric=metric,
            leaf_size=leaf_size,
            p=p,
            n_jobs=-1
        )
    elif classifier_name == 'SVC':
        svc_c = trial.params['svc_c']
        n_kernel = trial.params['kernel']

        classifier_obj = SVC(C=svc_c, kernel=n_kernel)
    elif classifier_name == 'Bayes':
        var_smoothing = trial.params['var_smoothing']

        classifier_obj = GaussianNB(
            var_smoothing=var_smoothing
        )
    elif classifier_name == 'Tree':
        max_depth = trial.params['max_depth']
        criterion = trial.params['criterion']
        splitter = trial.params['splitter']
        min_samples_split = trial.params['min_samples_split']
        min_samples_leaf = trial.params['min_samples_leaf']

        classifier_obj = DecisionTreeClassifier(
            max_depth=max_depth,
            criterion=criterion,
            splitter=splitter,
            min_samples_split=min_samples_split,
            min_samples_leaf=min_samples_leaf
        )
    elif classifier_name == 'RandomForest':
        max_depth = trial.params['max_depth']
        n_estimators = trial.params['n_estimators']
        max_features = trial.params['max_features']
        criterion = trial.params['criterion']
        min_samples_split = trial.params['min_samples_split']

        classifier_obj = RandomForestClassifier(
            max_depth=max_depth,
            n_estimators=n_estimators,
            max_features=max_features,
            criterion=criterion,
            min_samples_split=min_samples_split,
            n_jobs=-1
        )

    # statistics on the test data

    classifier_obj.fit(X_train, y_train)
    score = classifier_obj.score(X_test, y_test)
    print('Test score: {}'.format(score))

    y_pred = classifier_obj.predict(X_test)
    cm = confusion_matrix(y_test, y_pred)
    sns.heatmap(cm, annot=True, fmt='d')
    plt.title(f'{classifier_name} OPTUNA ConfusionMatrix')
    plt.show()

    y_pred_proba = classifier_obj.predict_proba(X_test)[::, 1]
    fpr, tpr, _ = roc_curve(y_test, y_pred_proba)
    auc = roc_auc_score(y_test, y_pred_proba)
    plt.plot(fpr, tpr, label=f'{classifier_name} auc=' + str(auc))
    plt.title(f'{classifier_name} OPTUNA ROCСurve')
    plt.legend(loc=4)

    plt.plot([0, 1], [0, 1], 'k--')
    plt.show()

    trials = study.trials_dataframe()
    trials.plot.scatter(x='number', y='value')
    plt.show()

    fig = optvis.plot_param_importances(study, params=list(study.best_params.keys()))
    fig.show()

def random_search_method(X_train, y_train, X_test, y_test):
    knn_param_grid = {
        'n_neighbors': list(range(1, 15)),
        'weights': ['uniform', 'distance'],
        'algorithm': ['auto', 'ball_tree', 'kd_tree', 'brute'],
        'metric': ['euclidean', 'manhattan', 'chebyshev', 'minkowski'],
        'leaf_size': list(range(1, 15)),
        'p': [1, 2]
    }

    svc_param_grid = {
        'C': uniform(1e-10, 1),
        'kernel': ['linear', 'poly', 'rbf', 'sigmoid']
    }

    bayes_param_grid = {
        'var_smoothing': uniform(1e-3, 1e-3)
    }

    tree_param_grid = {
        'max_depth': list(range(1, 32)),
        'criterion': ['gini', 'entropy'],
        'splitter': ['best', 'random'],
        'min_samples_split': list(range(2, 10)),
        'min_samples_leaf': list(range(1, 10))
    }

    random_forest_param_grid = {
        'max_depth': list(range(2, 32)),
        'n_estimators': list(range(1, 100)),
        'max_features': ['sqrt', 'log2'],
        'criterion': ['gini', 'entropy'],
        'min_samples_split': list(range(2, 10))
    }

    param_grid = {
        'KNN': knn_param_grid,
        'SVC': svc_param_grid,
        'Bayes': bayes_param_grid,
        'Tree': tree_param_grid,
        'RandomForest': random_forest_param_grid
    }

    classifiers = {
        'KNN': KNeighborsClassifier(),
        'SVC': SVC(),
        'Bayes': GaussianNB(),
        'Tree': DecisionTreeClassifier(),
        'RandomForest': RandomForestClassifier()
    }

    random_search_results = {}
    for alg in param_grid.keys():
        print(f'Training {alg} using RandomizedSearchCV...')
        random_search = RandomizedSearchCV(classifiers[alg],
                                           param_grid[alg],
                                           n_iter=100,
                                           cv=5,
                                           n_jobs=-1,
                                           random_state=42)
        random_search.fit(X_train, y_train)
        random_search_results[alg] = {'best_score': random_search.best_score_,
                                      'best_params': param_grid[alg],
                                      'best_estimator': random_search.best_estimator_}

    best_alg = None
    best_random = None
    best_random_score = -np.inf
    for alg, res in random_search_results.items():
        score = res['best_score']
        if score > best_random_score:
            best_random_score = score
            best_alg = alg
            best_random = res['best_estimator']

    print(f'Best performing model with random search: {best_random_score:.4f}')

    print("Best alg: {}".format(best_alg))
    print("Best parameters: {}".format(best_random.get_params()))

    score = best_random.score(X_test, y_test)
    print('Test score: {}'.format(score))

    y_pred = best_random.predict(X_test)
    cm = confusion_matrix(y_test, y_pred)
    sns.heatmap(cm, annot=True, fmt='d')
    plt.title(f'{best_alg} RandomSearch ConfusionMatrix')
    plt.show()

    y_pred_proba = best_random.predict_proba(X_test)[::, 1]
    fpr, tpr, _ = roc_curve(y_test, y_pred_proba)
    auc = roc_auc_score(y_test, y_pred_proba)
    plt.plot(fpr, tpr, label=f'{best_alg} auc=' + str(auc))
    plt.title(f'{best_alg} RandomSearch ROCCurve')
    plt.legend(loc=4)

    plt.plot([0, 1], [0, 1], 'k--')
    plt.show()


def my_print(line):
    print(line)
    print('-' * 20)


if __name__ == '__main__':
    main('PyCharm')
