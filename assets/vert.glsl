#version 400 core

in vec3 position;
in vec2 texture_coords;

out vec2 out_texture_coords;

void main(void) {
    gl_Position = vec4(position, 1.0);
    out_texture_coords = texture_coords;
}