import numpy as np
import pandas as pd
from sklearn.ensemble import AdaBoostClassifier
from sklearn.tree import DecisionTreeClassifier
from sklearn.model_selection import train_test_split
import matplotlib.pyplot as plt


def get_bounds(m_values):
    return m_values.min() - 0.5, m_values.max() + 0.5


# Повторите предыдущие два пункта, но вместо случайного леса используйте какой-нибудь алгоритм бустинга.

# 1. Постройте график зависимости качества классификации от числа деревьев
# на тестовой и тренировочной части набора данных.

df = pd.read_csv("chips.csv")

features = ['x', 'y']

X = df[features]
y = df['class']

X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.33, random_state=42)

ada_clf = AdaBoostClassifier(
    base_estimator=DecisionTreeClassifier(max_depth=1),
    n_estimators=100,
    learning_rate=0.5,
    random_state=42
)

ada_clf.fit(X_train.values, y_train.values)

train_scores = list(ada_clf.staged_score(X_train.values, y_train.values))
test_scores = list(ada_clf.staged_score(X_test.values, y_test.values))

plt.plot(train_scores, label="Train")
plt.plot(test_scores, label="Test")
plt.xlabel("Trees amount")
plt.ylabel("Classification Quality")
plt.legend()
plt.show()

# 2. Визуализируйте, как всё пространство классифицируется алгоритмом для разного числа деревьев.
# Должно получиться несколько картинок.
# Необязательно строить картинки для каждого шага.
# Можно выбрать несколько “интересных” номеров итераций, например: 1, 2, 3, 5, 8, 13, 21, 34 …

fig, axs = plt.subplots(2, 4, figsize=(30, 20))
axs, ax_index = axs.flatten(), 0

values_amount = 1000
x_min, x_max = get_bounds(X['x'])
y_min, y_max = get_bounds(X['y'])
xs = np.linspace(x_min, x_max, values_amount)
ys = np.linspace(y_min, y_max, values_amount)
xx, yy = np.meshgrid(xs, ys)

data = pd.DataFrame(np.c_[xx.ravel(), yy.ravel()], columns=features)

Zs = list(ada_clf.staged_predict(data.values))

iter_numbers = [1, 2, 3, 5, 8, 13, 21, 34]
for i, score in enumerate(Zs):
    if i in iter_numbers:
        Z = Zs[i].reshape(xx.shape)

        cmap = plt.cm.Paired

        axs[ax_index].pcolormesh(xx, yy, Z, cmap=cmap, alpha=0.5)
        axs[ax_index].scatter(X_train['x'], X_train['y'], c=y_train, cmap=cmap)
        axs[ax_index].set_xlabel('x')
        axs[ax_index].set_ylabel('y')
        axs[ax_index].set_title(f'AdaBoost Classification: iteration {i}')

        ax_index += 1

plt.show()
