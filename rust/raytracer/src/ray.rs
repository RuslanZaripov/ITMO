use crate::Vec3;

#[derive(Debug, Copy, Clone)]
pub struct Ray {
    pub origin: Vec3,
    pub dir: Vec3,
}

impl Ray {
    pub fn new(origin: Vec3, direction: Vec3) -> Self {
        Self { origin, dir: direction }
    }

    pub fn at(&self, factor: f64) -> Vec3 {
        self.origin + factor * self.dir
    }
}
