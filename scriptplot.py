import matplotlib.pyplot as plt
import csv

# Open the CSV file
agent_name = "UCT"
stats_name = ""
file_name = "UCT.csv" #"stats"+stats_name+".csv"
board = "12x12"
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
        data.append([float(x) for x in row[1:]])
# Create a figure and axis object
fig, ax = plt.subplots()

# Set the x-axis values
x_values = list(range(1, 1000, 100))

# Plot each data series
for i, d in enumerate(data):
    ax.plot(x_values, d, label=f"{bfactor_values[i]}")

# Add a legend and title
ax.legend()
ax.set_title(agent_name+stats_name+board)
ax.set_xlabel("arm pulls")
ax.set_ylabel(stats_name)

# Show the plot

plt.savefig(agent_name+"_stat"+stats_name+board+".png")
plt.show()