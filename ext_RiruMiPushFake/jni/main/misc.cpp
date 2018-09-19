#include <sys/types.h>
#include <unistd.h>

ssize_t fdgets(char *buf, const size_t size, int fd) {
    ssize_t len = 0;
    buf[0] = '\0';
    while (len < size - 1) {
        ssize_t ret = read(fd, buf + len, 1);
        if (ret < 0)
            return -1;
        if (ret == 0)
            break;
        if (buf[len] == '\0' || buf[len++] == '\n') {
            break;
        }
    }
    buf[len] = '\0';
    buf[size - 1] = '\0';
    return len;
}
