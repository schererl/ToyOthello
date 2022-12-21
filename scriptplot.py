import matplotlib.pyplot as plt
import csv

# Open the CSV file
with open("outputVar.csv", "r") as f:
    # Read the data from the file using the csv reader  
    reader = csv.reader(f, delimiter=";")
    # Extract the values from the input string
    bfactor_values = []
    data = []
    for row in reader:
        # skip the first row, which contains the headers
        if row[0] == "bfactor":
            continue
        # convert the values to float
        bfactor_values.append(float(row[0]))
        data.append([float(x) for x in row[11:]])

# Create a figure and axis object
fig, ax = plt.subplots()

# Set the x-axis values
x_values = list(range(1000, 10000, 100))

# Plot each data series
for i, d in enumerate(data):
    ax.plot(x_values, d, label=f"bfactor {bfactor_values[i]}")

# Add a legend and title
ax.legend()
ax.set_title("UCT reward variance growth")
ax.set_xlabel("arm pulls")
ax.set_ylabel("variance")

# Show the plot

plt.savefig("UCT_VarianceReward.png")
plt.show()