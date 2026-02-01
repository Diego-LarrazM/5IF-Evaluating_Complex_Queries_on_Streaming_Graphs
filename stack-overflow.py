import socket
import sys
import time
import csv

HOST = 'localhost'
# CSV_FILE = './source/elec.csv' or './source/synthetic_abrupt.csv'

def start_server(port):
    # Création du socket
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.bind((HOST, port))
    server_socket.listen(1)

    print(f"PYTHON: Le cerveau écoute sur {HOST}:{port}...")

    conn, addr = server_socket.accept()
    print(f"PYTHON: Connecté à Flink ({addr})")

    try:
        files = {
            "a2q": "./datasets/sx-stackoverflow-a2q.txt",
            "c2q": "./datasets/sx-stackoverflow-c2q.txt",
            "c2a": "./datasets/sx-stackoverflow-c2a.txt"
        }
        
        maxLines = 1000
        lines = 0

        # Open all three files
        with open(files["a2q"], 'r') as fileAQs, \
            open(files["c2q"], 'r') as fileCQs, \
            open(files["c2a"], 'r') as fileCAs:

            # Create CSV readers for each
            reader_a2q = csv.reader(fileAQs, delimiter=' ')
            reader_c2q = csv.reader(fileCQs, delimiter=' ')
            reader_c2a = csv.reader(fileCAs, delimiter=' ')

            # Skip headers if present
            # header = next(reader_a2q)
            # header = next(reader_c2q)
            # header = next(reader_c2a)

            # Loop line by line until any file ends
            for row_a2q, row_c2q, row_c2a in zip(reader_a2q, reader_c2q, reader_c2a):
                for row, label in zip([row_a2q, row_c2q, row_c2a], ['a2q', 'c2q', 'c2a']):
                    if row is None:
                        continue
                    # row should be [SRC, DST, TS]
                    src, dst, ts = row
                    event = f"{src};{dst};{label};{ts}\n"
                    conn.sendall(event.encode('utf-8'))
                    print(f"PYTHON: Sent: {event.strip()}")
                    time.sleep(0.05)  # optional delay between events
                    
                lines += 1 # Set a limit for testing
                if lines >= maxLines:
                    break

    except Exception as e:
        print(f"Erreur: {e}")
    finally:
        conn.close()
        server_socket.close()

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python stream_source.py <port>")
        sys.exit(1)
    
    port = int(sys.argv[1])
    start_server(port)