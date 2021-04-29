import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from matplotlib.cm import get_cmap, ScalarMappable
from matplotlib.colors import Normalize
from math import nan, isnan
import networkx as nx
import re
import statistics
import tabulate


def get_info_category() -> dict:
    return {'padec.attribute.SoundLevel': 1,'padec.attribute.Identity': 3, 'padec.attribute.Location': 2}


def parse_info(info: str) -> tuple:
    categories = get_info_category()
    parsed = re.search(
        "(padec.attribute.[a-zA-Z]*|History \(precision [-]?[0-9]*[\.]?[0-9Ee]+\)) from ([^,]*)", info)
    info_type = parsed.group(1)
    host = parsed.group(2)
    if info_type not in categories:
        precision_parsed = re.search(
            "precision ([-]?[0-9]*[\.]?[0-9Ee]+)", info_type)
        if precision_parsed is not None:
            prec_info_type = re.sub(
                " \(precision [-]?[0-9]*[\.]?[0-9Ee]+\)", "", info_type)
            precision = precision_parsed.group(1)
            return ('Precision', prec_info_type, float(precision), host)
    else:
        return ('Category', info_type, float(categories[info_type]), host)


def read_report(report_file: str):
    g = nx.Graph()
    with open(report_file, 'r') as in_report:
        rep_lines = list(map(lambda x: x.rstrip('\n'), in_report.readlines()))
    rep_lines = rep_lines[2:]  # Remove headers
    rep_lines = list(filter(lambda x: x != '', rep_lines)
                     )  # Remove empty lines
    for line in rep_lines:
        inf_re = re.search(": \[([^\]]*)\]", line)
        if inf_re is not None:
            infos = inf_re.group(1)
            if infos != '':
                party = re.sub(": \[[^\]]*\]", "", line)
                parsed_infos = list(
                    map(lambda x: parse_info(x), infos.split(", ")))
                if not g.has_node(party):
                    g.add_node(party)
                for p_info in parsed_infos:
                    if p_info[0] == 'Category':
                        if not g.has_edge(party, p_info[3]):
                            g.add_edge(
                                party, p_info[3], category=p_info[2], n_infos=1)
                        else:
                            if g.edges[party, p_info[3]]['category'] < p_info[2]:
                                g.edges[party, p_info[3]
                                        ]['category'] = p_info[2]
                            g.edges[party, p_info[3]]['n_infos'] += 1
                    else:
                        if g.nodes[party].get('precision') is None:
                            g.nodes[party]['precision'] = p_info[2]
                        else:
                            if g.nodes[party]['precision'] > p_info[2]:
                                g.nodes[party]['precision'] = p_info[2]
    return g


def get_max_val() -> float:
    return 1e3


def scale(nums: list) -> list:
    return list(map(lambda x: (x*100)/get_max_val() if x >= -1 else 500, nums))


def def_val(x: str) -> list:
    if x.startswith('C'):
        return 1
    else:
        return 2


def generate_legend_artist(color: str, label: str):
    return mpatches.Patch(color=color, label=label)


def draw_graph(g, limits: tuple = None, output: str = None):
    node_vals = [-1*g.nodes[x].get('precision', def_val(x)) for x in g.nodes]
    mv = get_max_val()
    node_vals = [v if v <= mv else mv for v in node_vals]
    node_vals = scale(node_vals)
    vmin = 0
    vmax = 100
    edge_vals = [g.edges[e]['category'] for e in g.edges]
    edge_vmin = 0
    edge_vmax = 3
    jet = get_cmap('jet')
    jet.set_under('black')
    jet.set_over('gray')
    viridis = get_cmap('viridis')
    nx.draw_networkx(g, with_labels=False, cmap=jet, edge_cmap=viridis, node_color=node_vals,
                     vmin=vmin, vmax=vmax, edge_color=edge_vals, edge_vmax=edge_vmax, edge_vmin=edge_vmin)
    clb1 = plt.colorbar(ScalarMappable(Normalize(vmin, vmax), jet))
    clb1.set_label('Maximum endpoint precision (%)')
    clb2 = plt.colorbar(ScalarMappable(
        Normalize(edge_vmin, edge_vmax), viridis))
    clb2.set_label('Maximum revealed category')
    plt.legend(handles=[generate_legend_artist(
        'grey', 'Provider'), generate_legend_artist('black', 'Denied access')])
    plt.tight_layout()
    if output is not None:
        if output.endswith('.eps'):
            plt.savefig(output, format='eps')
        else:
            plt.savefig(output)
    plt.show()


def draw_graph_2(reports: list, report_labels: list, limits: tuple = None, output: str = None):
    data_dkt = {'Average precision': [],
                'Average category': [],
                'Denied accesses': [],
                'Average attributes per key': []}
    for rep in reports:
        g = read_report(rep)
        node_vals = [-1*g.nodes[x].get('precision', def_val(x))
                     for x in g.nodes]
        mv = get_max_val()
        node_vals = [v if v <= mv else mv for v in node_vals]
        node_vals = scale(node_vals)
        denied_accesses = len(
            list(filter(lambda x: x < 0, node_vals)))*100/len(node_vals)
        node_vals = list(filter(lambda x: x >= 0 and x <= 100, node_vals))
        edge_vals = [g.edges[e]['category'] for e in g.edges]
        categories = [x*100/3 for x in edge_vals]
        infos = [g.edges[e]['n_infos'] for e in g.edges]
        infos = [x*100/3 for x in infos]
        data_dkt['Average precision'].append(statistics.mean(node_vals))
        data_dkt['Average category'].append(statistics.mean(categories))
        data_dkt['Denied accesses'].append(denied_accesses)
        data_dkt['Average attributes per key'].append(statistics.mean(infos))
    for key in data_dkt:
        plt.plot(list(range(len(data_dkt[key]))), data_dkt[key],
                 marker='o', linestyle='solid', label=key)
    plt.xticks(ticks=list(
        range(len(data_dkt['Average precision']))), labels=report_labels)
    plt.legend()
    plt.tight_layout(pad=0)
    plt.show()


def generate_table(reports: list, report_labels: list, limits: tuple = None, normalize: bool = True):
    data_dkt = {'Average precision': [],
                'Average category': [],
                'Denied accesses': [],
                'Average attributes per key': []}
    for rep in reports:
        print(rep)
        g = read_report(rep)
        node_vals = [-1*g.nodes[x].get('precision', def_val(x))
                     for x in g.nodes]
        mv = get_max_val()
        node_vals = [v if v <= mv else mv for v in node_vals]
        node_vals = scale(node_vals)
        if normalize:
            denied_accesses = len(
                list(filter(lambda x: x < 0, node_vals)))*100/len(node_vals)
        else:
            denied_accesses = len(list(filter(lambda x: x < 0, node_vals)))
        node_vals = list(filter(lambda x: x >= 0 and x <= 100, node_vals))
        edge_vals = [g.edges[e]['category'] for e in g.edges]
        if normalize:
            categories = [x*100/3 for x in edge_vals]
        else:
            categories = edge_vals
        infos = [g.edges[e]['n_infos'] for e in g.edges]
        if normalize:
            infos = [x*100/3 for x in infos]
        data_dkt['Average precision'].append(statistics.mean(node_vals))
        data_dkt['Average category'].append(statistics.mean(categories))
        data_dkt['Denied accesses'].append(denied_accesses)
        data_dkt['Average attributes per key'].append(statistics.mean(infos))
    table_header = ["Step"]
    table_header.extend(list(data_dkt.keys()))
    table_fmt = [table_header]
    for ndx, rep in enumerate(report_labels):
        rep_line = [rep]
        for key in data_dkt:
            rep_line.append(data_dkt[key][ndx])
        table_fmt.append(rep_line)
    return tabulate.tabulate(table_fmt, tablefmt='html')

if __name__ == "__main__":
    draw_graph_2(['reports/NYCDemoIncremental/step1_rbac_demo_PADECPartyReport.txt',
                  'reports/NYCDemoIncremental/step1_padec_demo_PADECPartyReport.txt',
                  'reports/NYCDemoIncremental/step3_abac_demo_PADECPartyReport.txt',
                  'reports/NYCDemoIncremental/step2_padec_demo_PADECPartyReport.txt',
                  'reports/NYCDemoIncremental/step3_padec_demo_PADECPartyReport.txt',
                  'reports/NYCDemoIncremental/step4_twokh_demo_PADECPartyReport.txt',
                  'reports/NYCDemoIncremental/step5_filter_demo_PADECPartyReport.txt',
                  'reports/NYCDemoIncremental/step6_demo_PADECPartyReport.txt'],
                 ['RBAC',
                  'Step 1',
                  'ABAC',
                  'Step 2',
                  'Step 3',
                  'Step 4',
                  'Step 5',
                  'Step 6'])
