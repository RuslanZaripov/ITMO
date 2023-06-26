import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
import matplotlib.pyplot as plt
import numpy as np


def get_bounds(m_values):
    return m_values.min() - 0.5, m_values.max() + 0.5


# 1. Постройте график зависимости качества классификации от числа деревьев
# на тестовой и тренировочной части набора данных.

df = pd.read_csv("chips.csv")

features = ['x', 'y']

X = df[features]
y = df['class']

X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.33, random_state=42)

tree_amounts = [1, 2, 3, 5, 8, 13, 21, 34]

scores = {'train_score': [], 'test_score': []}

for tree_amount in tree_amounts:
    model = RandomForestClassifier(n_estimators=tree_amount, random_state=42)
    model.fit(X_train, y_train)

    scores['train_score'].append(model.score(X_train, y_train))
    scores['test_score'].append(model.score(X_test, y_test))

plt.plot(tree_amounts, scores['train_score'], label='Train')
plt.plot(tree_amounts, scores['test_score'], label='Test')
plt.xlabel('Trees amount')
plt.ylabel('Classification Quality')
plt.legend()
plt.show()

# 2. Визуализируйте, как всё пространство классифицируется алгоритмом для разного числа деревьев.
# Должно получиться несколько картинок. Необязательно строить картинки для каждого шага.
# Можно выбрать несколько “интересных” номеров итераций, например: 1, 2, 3, 5, 8, 13, 21, 34 …

fig, axs = plt.subplots(2, 4, figsize=(30, 20))
axs = axs.flatten()

values_amount = 1000
x_min, x_max = get_bounds(X['x'])
y_min, y_max = get_bounds(X['y'])
xs = np.linspace(x_min, x_max, values_amount)
ys = np.linspace(y_min, y_max, values_amount)
xx, yy = np.meshgrid(xs, ys)

values = pd.DataFrame(np.c_[xx.ravel(), yy.ravel()], columns=features)

for ax_index, tree_amount in enumerate(tree_amounts):
    model = RandomForestClassifier(n_estimators=tree_amount, random_state=42)
    model.fit(X, y)

    Z = model.predict(values)
    Z = Z.reshape(xx.shape)

    cmap = plt.cm.Paired

    axs[ax_index].pcolormesh(xx, yy, Z, cmap=cmap, alpha=0.5)
    axs[ax_index].scatter(X_train['x'], X_train['y'], c=y_train, cmap=cmap)
    axs[ax_index].set_xlabel('x')
    axs[ax_index].set_ylabel('y')
    axs[ax_index].set_title(f'Random forest: {tree_amount} trees')

plt.show()
