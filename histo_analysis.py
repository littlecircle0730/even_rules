import os
import json
from datetime import datetime
from statistics import mean

def read_histos(folder) -> dict:
    h_names = os.listdir(folder)
    return {h_name: os.path.join(folder, h_name) for h_name in h_names}

def histo_stats(histo_path) -> tuple:
    with open(histo_path, 'r') as h_file:
        histo = json.load(h_file)
    stat_length = len(histo)
    start_date = datetime.strptime("2030-12-31", "%Y-%m-%d").date()
    end_date = datetime.strptime("1990-01-01", "%Y-%m-%d").date()
    for element in histo:
        h_date = datetime.strptime(element['time'], "%Y-%m-%d").date()
        if h_date < start_date:
            start_date = h_date
        if h_date > end_date:
            end_date = h_date
    return (stat_length, start_date, end_date)


def main():
    FOLDER = 'padec_history'
    BANNED_HISTOS = ['Histo1_madeup.json']
    histos = read_histos(FOLDER)
    for ban in BANNED_HISTOS:
        histos.pop(ban)
    histo_list = list(histos.values())
    start_date = datetime.strptime("2030-12-31", "%Y-%m-%d").date()
    end_date = datetime.strptime("1990-01-01", "%Y-%m-%d").date()
    lengths = []
    for histo in histo_list:
        h_len, h_start, h_end = histo_stats(histo)
        lengths.append(h_len)
        if h_start < start_date:
            start_date = h_start
        if h_end > end_date:
            end_date = h_end
    print("Average length: {}".format(mean(lengths)))
    print("Total length: {}".format(sum(lengths)))
    print("Starting date: {}".format(start_date.strftime("%Y-%m-%d")))
    print("Ending date: {}".format(end_date.strftime("%Y-%m-%d")))



if __name__ == "__main__":
    main()
