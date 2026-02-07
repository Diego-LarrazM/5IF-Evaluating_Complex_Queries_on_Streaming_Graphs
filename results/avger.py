import os
import re
import matplotlib.pyplot as plt

# Root folder containing W10Q1, W10Q2, ..., W30Q3
root_folder = "."  # <-- replace with your root path

# Patterns
snapshot_pattern = re.compile(r"SnapshotSize:\s*(\d+)")
throughput_pattern = re.compile(r"Throughput:\s*([\d.]+) events/sec")

# Dictionary to store results per query folder
results = {}

# Loop through each query folder
for query_folder in sorted(os.listdir(root_folder)):
    folder_path = os.path.join(root_folder, query_folder)
    if os.path.isdir(folder_path):
        snapshot_sizes = []
        throughputs = []

        # Loop through all results_*.txt files
        for filename in sorted(os.listdir(folder_path)):
            if filename.startswith("results") and filename.endswith(".txt"):
                file_path = os.path.join(folder_path, filename)
                with open(file_path, "r") as f:
                    content = f.read()

                    # Extract SnapshotSize
                    snapshot_match = snapshot_pattern.search(content)
                    if snapshot_match:
                        snapshot_sizes.append(int(snapshot_match.group(1)))
                    else:
                        snapshot_sizes.append(None)

                    # Extract Throughput
                    throughput_match = throughput_pattern.search(content)
                    if throughput_match:
                        throughputs.append(float(throughput_match.group(1)))
                    else:
                        throughputs.append(None)

        # Store in the dictionary
        results[query_folder] = {
            "snapshot_sizes": snapshot_sizes,
            "throughputs": throughputs
        }

ThrouOutput = {"Q1": [], "Q2": [], "Q3": []}
Snaps =       {"Q1": [], "Q2": [], "Q3": []}
Ws =          [10,       20,       30]
for key, dic in results.items():
  query = "Q" + key.split("Q")[1]
  
  sAvg = sum(dic["snapshot_sizes"])/len(dic["snapshot_sizes"])
  thAvg = sum(dic["throughputs"])/len(dic["throughputs"])
  ThrouOutput[query].append(thAvg)
  Snaps[query].append(sAvg)
  print(key, query, thAvg, sAvg)

# --- Plot 1: Throughput ---
plt.figure(figsize=(6,4))
for query in ['Q1','Q2','Q3']:
    plt.plot(Ws, ThrouOutput[query], marker='o', label=query)
plt.xlabel('WindowSize')
plt.ylabel('Throughput (events/s)')
plt.title('Throughput vs WindowSize')
plt.legend()
plt.grid(True)
plt.tight_layout()
plt.savefig('Throughput_plot.png')
plt.close()

# --- Plot 2: SnapshotSize ---
plt.figure(figsize=(6,4))
for query in ['Q1','Q2','Q3']:
    plt.plot(Ws, Snaps[query], marker='s', linestyle='--', label=query)
plt.xlabel('WindowSize')
plt.ylabel('SnapshotSize (#sgts)')
plt.title('SnapshotSize vs WindowSize')
plt.legend()
plt.grid(True)
plt.tight_layout()
plt.savefig('SnapshotSize_plot.png')
plt.close()

print("Plots saved as Throughput_plot.png and SnapshotSize_plot.png")