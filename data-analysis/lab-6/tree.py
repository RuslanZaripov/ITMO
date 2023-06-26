import numpy as np
import pandas as pd
from sklearn.tree import DecisionTreeClassifier
from sklearn.model_selection import train_test_split
import matplotlib.pyplot as plt
from sklearn.tree import plot_tree
from sklearn.metrics import accuracy_score, confusion_matrix, precision_score, recall_score

# 1. Постройте дерево принятия решений на заданном наборе данных.

data = pd.read_csv('chips.csv')

features = ['x', 'y']

X = data[features]
y = data['class']

X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.33, random_state=42)

clf = DecisionTreeClassifier()
clf.fit(X_train, y_train)
print(f'Params:\n{clf.get_params()}')

y_pred = clf.predict(X_test)

# 2. Визуализируйте полученное дерево.

plt.figure(figsize=(60, 30))
plot_tree(clf, filled=True, class_names=['0', '1'], feature_names=features)
plt.show()

print(f'Accuracy:\n{accuracy_score(y_test, y_pred)}')
print(f'Confusion matrix:\n{confusion_matrix(y_test, y_pred, labels=[0, 1])}')
print(f'Precision:\n{precision_score(y_test, y_pred)}')
print(f'Recall:\n{recall_score(y_test, y_pred)}')

importances = pd.DataFrame(
    {'feature': X_train.columns, 'importance': np.round(clf.feature_importances_, 3)}) \
    .sort_values('importance', ascending=False)
print(f'Feature importances:\n{importances}')

# 3. Постройте график зависимости качества классификации от глубины дерева принятия решений
# на тестовой и тренировочной части набора данных.

depths = np.arange(1, 20)

scores = {'train_score': [], 'test_score': []}

for depth in depths:
    clf = DecisionTreeClassifier(max_depth=depth)

    clf.fit(X_train, y_train)

    scores['train_score'].append(clf.score(X_train, y_train))
    scores['test_score'].append(clf.score(X_test, y_test))

plt.plot(depths, scores['train_score'], label='Train')
plt.plot(depths, scores['test_score'], label='Test')
plt.xlabel('Tree depth')
plt.ylabel('Classification quality')
plt.legend()
plt.show()
