use crate::Vec3;

#[derive(Debug, Copy, Clone)]
pub struct Material {
    pub color: Vec3,
}

impl Material {
    pub fn new(color: Vec3) -> Material {
        Material { color }
    }
}