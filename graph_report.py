import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from matplotlib.cm import get_cmap, ScalarMappable
from matplotlib.colors import Normalize
from math import nan, isnan
import networkx as nx
import re

def get_info_category() -> dict:
    return {'padec.attribute.Identity': 3, 'padec.attribute.Location': 2}

def parse_info(info: str) -> tuple:
    categories = get_info_category()
    parsed = re.search(
        "(padec.attribute.[a-zA-Z]*|History \(precision [-]?[0-9]*[\.]?[0-9Ee]+\)) from ([^,]*)", info)
    info_type = parsed.group(1)
    host = parsed.group(2)
    if info_type not in categories:
        precision_parsed = re.search("precision ([-]?[0-9]*[\.]?[0-9Ee]+)", info_type)
        if precision_parsed is not None:
            prec_info_type = re.sub(" \(precision [-]?[0-9]*[\.]?[0-9Ee]+\)", "", info_type)
            precision = precision_parsed.group(1)
            return ('Precision', prec_info_type, float(precision), host)
    else:
        return ('Category', info_type, float(categories[info_type]), host)

def read_report(report_file: str):
    g = nx.Graph()
    with open(report_file, 'r') as in_report:
        rep_lines = list(map(lambda x: x.rstrip('\n'), in_report.readlines()))
    rep_lines = rep_lines[2:] # Remove headers
    rep_lines = list(filter(lambda x: x!='', rep_lines)) # Remove empty lines
    for line in rep_lines:
        inf_re = re.search(": \[([^\]]*)\]", line)
        if inf_re is not None:
            infos = inf_re.group(1)
            if infos != '':
                party = re.sub(": \[[^\]]*\]", "", line)
                parsed_infos = list(map(lambda x: parse_info(x), infos.split(", ")))
                if not g.has_node(party):
                    g.add_node(party)
                for p_info in parsed_infos:
                    if p_info[0] == 'Category':
                        if not g.has_edge(party, p_info[3]):
                            g.add_edge(party, p_info[3], category=p_info[2])
                        else:
                            if g.edges[party, p_info[3]]['category'] < p_info[2]:
                                g.edges[party, p_info[3]]['category'] = p_info[2]
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
    nx.draw_networkx(g, with_labels=False, cmap=jet, edge_cmap=viridis, node_color=node_vals, vmin=vmin, vmax=vmax, edge_color=edge_vals, edge_vmax=edge_vmax, edge_vmin=edge_vmin)
    clb1 = plt.colorbar(ScalarMappable(Normalize(vmin, vmax), jet))
    clb1.set_label('Maximum endpoint precision (%)')
    clb2 = plt.colorbar(ScalarMappable(Normalize(edge_vmin, edge_vmax), viridis))
    clb2.set_label('Maximum revealed category')
    plt.legend(handles=[generate_legend_artist('grey', 'Provider'), generate_legend_artist('black', 'Denied access')])
    plt.tight_layout()
    if output is not None:
        if output.endswith('.eps'):
            plt.savefig(output, format='eps')
        else:
            plt.savefig(output)
    plt.show()

if __name__ == "__main__":
    draw_graph(read_report('reports/Demo/padec_demo_PADECPartyReport.txt'), output='HelsinkiPADEC.png')
    draw_graph(read_report('reports/Demo/rbac_padec_demo_PADECPartyReport.txt'), output='HelsinkiRBAC.png')
    draw_graph(read_report('reports/Demo/abac_padec_demo_PADECPartyReport.txt'), output='HelsinkiABAC.png')
