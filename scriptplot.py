import matplotlib.pyplot as plt
import csv

# Open the CSV file
stats_name = "VarCoef"
file_name = "stats"+stats_name+".csv"
board = "24x24"
with open(file_name, "r") as f:
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
        bfactor_values.append(str(row[0]))
        data.append([float(x) for x in row[11:]])
# Create a figure and axis object
fig, ax = plt.subplots()

# Set the x-axis values
x_values = list(range(1000, 25000, 100))

# Plot each data series
for i, d in enumerate(data):
    ax.plot(x_values, d, label=f"{bfactor_values[i]}")

# Add a legend and title
ax.legend()
ax.set_title("UCT"+stats_name+board)
ax.set_xlabel("arm pulls")
ax.set_ylabel(stats_name)

# Show the plot

plt.savefig("UCT_stat"+stats_name+board+".png")
plt.show()