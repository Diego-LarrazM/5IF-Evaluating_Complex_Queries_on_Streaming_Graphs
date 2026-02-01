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
        # Open all three files
        with open("./datasets/integration_test.csv", 'r') as fileTest:

            # Create CSV readers for each
            reader = csv.reader(fileTest, delimiter=';')

            # Loop line by line until any file ends
            for row in reader:
                if row is None:
                    continue
                    
                src, dst, label, ts = row
                event = f"{src};{dst};{label};{ts}\n"
                conn.sendall(event.encode('utf-8'))
                print(f"PYTHON: Sent: {event.strip()}")
                time.sleep(1)  # optional delay between events

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