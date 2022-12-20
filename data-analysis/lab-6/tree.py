# Decision Trees
#
# Task
#
#
# Tree:
# 1. Build a decision tree on the given dataset
#    (chips.csv file with columns: x (float from -1 to 1), y (float from -1 to 1), class (0 or 1)).
# 2. Visualize the resulting tree.
# 3. Plot classification quality versus decision tree depth on the test
#    and training parts of the dataset.
#
# Random Forest:
# 1. Plot the classification quality
#    versus the number of trees on the test and training parts of the dataset.
# 2. Visualize how the entire space is classified by the algorithm for a different number of trees.
#    There should be several pictures. It is not necessary to build pictures for each step.
#    You can select several “interesting” iteration numbers,
#    for example: 1, 2, 3, 5, 8, 13, 21, 34...
#
# Boosting:
# 1. Repeat the previous two points,
#    but use some kind of boosting algorithm instead of a random forest.
import numpy as np
# Visualization of the classification of the entire space:
# - The figure shows an example for a different algorithm (SVM) and a different data set.
# - Each point in space must be classified and colored in the appropriate color.
# - It is not necessary to use a gradient (smooth color transition) for this.
# - For the background it is better to use lighter/less saturated colors.
# - Don't forget to add points from the training dataset.

# Import the libraries
import pandas as pd
from sklearn.tree import DecisionTreeClassifier
from sklearn.model_selection import train_test_split
import matplotlib.pyplot as plt
from sklearn.tree import plot_tree
from sklearn.metrics import accuracy_score

# Load the data
data = pd.read_csv('chips.csv')

features = ['x', 'y']

# Split the data into features and target
X = data[features]
y = data['class']

# Split the data into train and test sets
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.33, random_state=42)

# Tree:
# 1. Build a decision tree on the given dataset
# Create a decision tree classifier
clf = DecisionTreeClassifier()

# Train the classifier
clf.fit(X_train, y_train)

print(f'Params:\n{clf.get_params()}')

# Predict the test set labels
y_pred = clf.predict(X_test)

# 2. Visualize the resulting tree.
# Visualize the decision tree
plt.figure(figsize=(60, 30))
plot_tree(clf, filled=True, class_names=['0', '1'], feature_names=features)
plt.show()

# print accuracy
print(f'Accuracy:\n{accuracy_score(y_test, y_pred)}')

# print confusion matrix
from sklearn.metrics import confusion_matrix

print(f'Confusion matrix:\n{confusion_matrix(y_test, y_pred, labels=[0, 1])}')

# print precision
from sklearn.metrics import precision_score

print(f'Precision:\n{precision_score(y_test, y_pred)}')

# print recall
from sklearn.metrics import recall_score

# recall is also known as sensitivity or true positive rate (TPR)
print(f'Recall:\n{recall_score(y_test, y_pred)}')

importances = pd.DataFrame({'feature': X_train.columns, 'importance': np.round(clf.feature_importances_, 3)})
importances = importances.sort_values('importance', ascending=False)
print(f'Feature importances:\n{importances}')

# 3. Plot classification quality versus decision tree depth on the test
# and training parts of the dataset.

# List of tree depths to test
depths = np.arange(1, 20)

# Lists to store accuracy scores for train and test sets
scores = {'train_score': [], 'test_score': []}

# Loop through tree depths
for depth in depths:
    # Create a decision tree classifier with the current depth
    clf = DecisionTreeClassifier(max_depth=depth)

    # Fit the classifier to the training data
    clf.fit(X_train, y_train)

    # Predict on train and test sets
    # Calculate and store accuracy scores
    scores['train_score'].append(clf.score(X_train, y_train))
    scores['test_score'].append(clf.score(X_test, y_test))

# Plot the results
plt.plot(depths, scores['train_score'], label='Train')
plt.plot(depths, scores['test_score'], label='Test')
plt.xlabel('Tree depth')
plt.ylabel('Classification quality')
plt.legend()
plt.show()
