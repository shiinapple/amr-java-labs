package lab;

/** 有问题的一对路径。先后顺序与输入列表中的路径顺序相同。 */
public record Conflict(String firstPathId, String secondPathId) {}
