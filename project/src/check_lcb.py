
def main():
    with open('membership', 'r') as f:
        lines = f.readlines()
        nbr_proc = int(lines[0].rstrip())
        affected = []
        for i in range(nbr_proc):
            split = lines[i+nbr_proc+1].rstrip().split(' ')
            split = [int(x) for x in split]
            affected.append(split)

    #Read files
    files = []
    for i in range(nbr_proc):
        with open('da_proc_{}.out'.format(i+1), 'r') as f:
            files.append([x.rstrip() for x in f.readlines()])

    empty = all([len(x) == 0 for x in files])
    if(empty):
        print('All input files are empty.')
        exit()

    #Create dependencies
    dependencies = {}
    for i, f in enumerate(files):
        dep = []
        for e in f:

            if(e.startswith('d')):
                split = e.split(' ')
                broadcaster = int(split[1])
                if(broadcaster == i+1 or broadcaster in affected[i]):
                    dep.append((broadcaster, int(split[2])))
            elif(e.startswith('b')):
                split = e.split(' ')
                dependencies[(i+1, int(split[1]))] = dep.copy()


    #Check dependencies
    for i, f in enumerate(files):
        delivered = []
        for e in f:
            if(e.startswith('d')):
                split = e.split(' ')
                broadcaster = int(split[1])
                value = int(split[2])
                dep = dependencies[(broadcaster, value)]
                for d in dep:
                    if(not(d in delivered)):
                        print('Error: p{} delivers ({}, {}) before {}'.format(i+1, broadcaster, value, d))
                        exit()
                delivered.append((broadcaster,value))
    print('LCB properties fulfilled.')


if __name__ == '__main__':
    main()
