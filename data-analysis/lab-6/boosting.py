import numpy as np
import pandas as pd
from sklearn.ensemble import AdaBoostClassifier
from sklearn.tree import DecisionTreeClassifier
from sklearn.model_selection import train_test_split
import matplotlib.pyplot as plt


def get_bounds(m_values):
    return m_values.min() - 0.5, m_values.max() + 0.5


# Load the chips dataset
df = pd.read_csv("chips.csv")

features = ['x', 'y']

# Split the data into X (features) and y (target)
X = df[features]
y = df['class']

# Split the data into training and test sets
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.33, random_state=42)

# Create the AdaBoost classifier
ada_clf = AdaBoostClassifier(
    base_estimator=DecisionTreeClassifier(max_depth=1),
    n_estimators=200,
    learning_rate=0.5,
    random_state=42
)

# Train the classifier
ada_clf.fit(X_train, y_train)

# Evaluate the classifier on the training and test sets
train_scores = list(ada_clf.staged_score(X_train, y_train))
test_scores = list(ada_clf.staged_score(X_test, y_test))

# Plot the classification quality versus the number of trees
plt.plot(train_scores, label="Train")
plt.plot(test_scores, label="Test")
plt.xlabel("Trees Number")
plt.ylabel("Classification Quality")
plt.legend()
plt.show()

fig, axs = plt.subplots(2, 4, figsize=(30, 20))
axs = axs.flatten()
ax_index = 0

# Create a mesh of points in the feature space
values_amount = 1000
x_min, x_max = get_bounds(X['x'])
y_min, y_max = get_bounds(X['y'])
xs = np.linspace(x_min, x_max, values_amount)
ys = np.linspace(y_min, y_max, values_amount)
xx, yy = np.meshgrid(xs, ys)

values = pd.DataFrame(np.c_[xx.ravel(), yy.ravel()], columns=features)

# Predict the class of each point in the mesh
Zs = list(ada_clf.staged_predict(values))

iter_numbers = [1, 2, 3, 5, 8, 13, 21, 34]
# Iterate over the number of trees and generate a prediction for each point in the mesh
for i, score in enumerate(Zs):

    # Select a few "interesting" iteration numbers to visualize
    if i in iter_numbers:
        # Reshape the prediction to match the shape
        Z = Zs[i].reshape(xx.shape)

        cmap = plt.cm.Paired

        # Create a new figure and plot the prediction
        axs[ax_index].pcolormesh(xx, yy, Z, cmap=cmap, alpha=0.5)

        # Overlay the training points on the plot
        axs[ax_index].scatter(X_train['x'], X_train['y'], c=y_train, cmap=cmap)

        # Set the plot limits and labels
        axs[ax_index].set_xlabel('x')
        axs[ax_index].set_ylabel('y')
        axs[ax_index].set_title(f'AdaBoost Classification: iteration {i}')

        ax_index += 1

# Show the plot
plt.show()
