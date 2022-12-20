import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
import matplotlib.pyplot as plt
import numpy as np


def get_bounds(m_values):
    return m_values.min() - 0.5, m_values.max() + 0.5


# Load the chips dataset
df = pd.read_csv("chips.csv")

features = ['x', 'y']

# Split the data into X (features) and y (target)
X = df[features]
y = df['class']

# Split the data into a training set and a test set
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.33, random_state=42)

# Create a list of the number of trees to evaluate
tree_list = [1, 2, 3, 5, 8, 13, 21, 34]

# Create a dictionary to store the training and test scores
scores = {'train_score': [], 'test_score': []}

# Iterate through the list of trees and fit a random forest model
for num_trees in tree_list:
    model = RandomForestClassifier(n_estimators=num_trees, random_state=42)

    model.fit(X_train, y_train)

    # Get the training and test scores for the model
    scores['train_score'].append(model.score(X_train, y_train))
    scores['test_score'].append(model.score(X_test, y_test))

# Plot the classification quality versus the number of trees on the test and training parts of
# the dataset
plt.plot(tree_list, scores['train_score'], label='Train')
plt.plot(tree_list, scores['test_score'], label='Test')
plt.xlabel('Trees Number')
plt.ylabel('Classification Quality')
plt.legend()
plt.show()

# Visualize how the entire space is classified by the algorithm for a different number of trees

# Create a figure with subplots
fig, axs = plt.subplots(2, 4, figsize=(30, 20))
axs = axs.flatten()

values_amount = 1000
x_min, x_max = get_bounds(X['x'])
y_min, y_max = get_bounds(X['y'])
xs = np.linspace(x_min, x_max, values_amount)
ys = np.linspace(y_min, y_max, values_amount)
xx, yy = np.meshgrid(xs, ys)

# Wrap ravel_ in a DataFrame with the correct column names
values = pd.DataFrame(np.c_[xx.ravel(), yy.ravel()], columns=features)

# Iterate through the iteration numbers and plot the classification of the entire space
for ax_index, num_trees in enumerate(tree_list):
    model = RandomForestClassifier(n_estimators=num_trees, random_state=42)
    model.fit(X, y)

    # Get the predictions for each point in the meshgrid
    Z = model.predict(values)
    Z = Z.reshape(xx.shape)

    cmap = plt.cm.coolwarm

    # Plot the meshgrid and the points from the training set
    axs[ax_index].pcolormesh(xx, yy, Z, cmap=cmap, alpha=0.8)
    axs[ax_index].scatter(X_train['x'], X_train['y'], c=y_train, cmap=cmap)
    axs[ax_index].set_xlabel('x')
    axs[ax_index].set_ylabel('y')
    axs[ax_index].set_title(f'Random forest: {num_trees} trees')

# Show the plot
plt.show()
